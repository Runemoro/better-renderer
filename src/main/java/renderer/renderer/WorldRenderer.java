package renderer.renderer;

import org.joml.Vector3d;
import renderer.cache.CacheSystem;
import renderer.model.ModelDefinition;
import renderer.model.TransformDefinition;
import renderer.util.Util;
import renderer.world.*;

import java.util.Collections;
import java.util.List;

public class WorldRenderer {
    public static final double SCALE = 1 / 128.;
    public static final int CHUNK_SIZE = Renderer.CHUNK_SIZE;
    public final BufferBuilder opaqueBuffer = new BufferBuilder(2500);
    public final BufferBuilder translucentBuffer = new BufferBuilder(100);
    public final World world;

    public WorldRenderer(World world) {
        this.world = world;
    }

    public void chunk(int chunkX, int chunkY) {
        int x1 = chunkX * CHUNK_SIZE;
        int y1 = chunkY * CHUNK_SIZE;
        int x2 = x1 + CHUNK_SIZE;
        int y2 = y1 + CHUNK_SIZE;

        triangle(new Vector3d(x2, y2, -10), new Vector3d(x1, y2, -10), new Vector3d(x1, y1, -10), 0, 0xff, 0);
        triangle(new Vector3d(x1, y1, -10), new Vector3d(x2, y1, -10), new Vector3d(x2, y2, -10), 0, 0xff, 0);

        for (int plane = 0; plane < 4; plane++) {
            for (int x = x1; x < x2; x++) {
                for (int y = y1; y < y2; y++) {
                    if (plane >= world.roofRemovalPlane && world.roofsRemoved.contains(new Position(x, y, 0))) {
                        continue;
                    }

                    tile(plane, x, y);
                }
            }
        }

        for (Location location : world.locations(x1 / 64, y1 / 64)) {
            int plane = location.position.z;
            int x = location.position.x;
            int y = location.position.y;

            if (x >= x1 && x < x2 && y >= y1 && y < y2) {
                int actualZ = location.position.z;

                if ((world.settings(x, y, plane) & 0x2) != 0) {
                    actualZ--; // bridge, shift down
                }

                if ((world.settings(x, y, plane) & 0x8) != 0) {
                    actualZ = 0; // arch, always render (at the ge for example)
                }


                if (actualZ >= world.roofRemovalPlane && world.roofsRemoved.contains(new Position(x, y, 0))) {
                    continue;
                }

                object(location.object, location.type, plane, x, y, location.rotation);
            }
        }
    }

    public void tile(int plane, int x, int y) {
        UnderlayDefinition underlay = world.underlay(x, y, plane);
        OverlayDefinition overlay = world.overlay(x, y, plane);

        if (underlay != null && overlay == null) {
            groundSquare(plane, x, y, -1);
            return;
        }

        if (overlay != null) {
            int color = overlay.texture == null ? overlay.color : overlay.texture.averageColor;

            if (overlay.color == 0xff00ff) {
                color = -1;
            }

            OverlayShape shape = world.getOverlayShape(x, y, plane);
            int rotation = world.getOverlayRotation(x, y, plane);

            if (shape == OverlayShape.FULL) {
                if (color != -1) {
                    groundSquare(plane, x, y, color);
                }
                return;
            }

            for (OverlayShape.Triangle triangle : shape.triangles) {
                Vector3d a = triangle.positionA().rotateZ(-Math.PI / 2 * rotation).add(x + 0.5, y + 0.5, 0);
                Vector3d b = triangle.positionB().rotateZ(-Math.PI / 2 * rotation).add(x + 0.5, y + 0.5, 0);
                Vector3d c = triangle.positionC().rotateZ(-Math.PI / 2 * rotation).add(x + 0.5, y + 0.5, 0);

                if (triangle.overlay && overlay != null && color != -1) {
                    groundVertex(plane, a.x, a.y, color);
                    groundVertex(plane, b.x, b.y, color);
                    groundVertex(plane, c.x, c.y, color);
                }

                if (!triangle.overlay && underlay != null) {
                    groundVertex(plane, a.x, a.y, -1);
                    groundVertex(plane, b.x, b.y, -1);
                    groundVertex(plane, c.x, c.y, -1);
                }
            }
        }
    }

    public void groundSquare(int plane, int x, int y, int color) {
        double h00 = world.height(x, y, plane);
        double h01 = world.height(x, y + 1, plane);
        double h10 = world.height(x + 1, y, plane);
        double h11 = world.height(x + 1, y + 1, plane);

        if (Math.abs(h00 - h11) > Math.abs(h10 - h01)) {
            groundVertex(plane, x + 1, y + 1, color);
            groundVertex(plane, x, y + 1, color);
            groundVertex(plane, x + 1, y, color);

            groundVertex(plane, x, y, color);
            groundVertex(plane, x + 1, y, color);
            groundVertex(plane, x, y + 1, color);
        } else {
            groundVertex(plane, x + 1, y + 1, color);
            groundVertex(plane, x, y + 1, color);
            groundVertex(plane, x, y, color);

            groundVertex(plane, x + 1, y + 1, color);
            groundVertex(plane, x, y, color);
            groundVertex(plane, x + 1, y, color);
        }
    }

    public void groundVertex(int plane, double x, double y, int color) {
        if (color == -1) {
            color = world.color(x, y, plane);
        }

        opaqueBuffer.vertex(world.position(x, y, plane), world.normal(x, y, plane), 0xff000000 | color, plane * 20);
    }

    public void groundVertex(int plane, int x, int y, int color) {
        if (color == -1) {
            color = world.color(x, y, plane);
        }

        opaqueBuffer.vertex(world.position(x, y, plane), world.normal(x, y, plane), 0xff000000 | color, plane * 20);
    }

    public void object(ObjectDefinition object, LocationType type, int plane, int x, int y, int rotation) {
        if (object.animation != null) {
            return;
        }

        if (object.models == null && object.typeModels == null) {
            return;
        }

        List<ModelDefinition> models = null;

        if (object.typeModels != null) {
            ModelDefinition model = object.typeModels.get(type.baseType);

            if (model != null) {
                models = Collections.singletonList(model);
            }
        }

        if (models == null && object.models != null) {
            models = object.models;
        }

        if (models == null || models.isEmpty()) {
            return;
        }

        // flip
        boolean flip = object.mirror;

        // rotate

        if (rotation > 3) {
            throw new UnsupportedOperationException("nyi");
        }

        rotation %= 4;
        double angle = -Math.PI / 2 * rotation;

        if (type == LocationType.OBJECT_DIAGONAL || type == LocationType.WALL_DECORATION_DIAGONAL || type == LocationType.WALL_DECORATION_OPPOSITE_DIAGONAL || type == LocationType.WALL_DECORATION_DOUBLE) {
            angle += -Math.PI / 4;
        }

        // scale
        Vector3d scale = new Vector3d(object.scaleX / 128., object.scaleY / 128., object.scaleZ / 128.);

        // translate
        int sizeX = rotation == 0 || rotation == 2 ? object.sizeX : object.sizeY;
        int sizeY = rotation == 0 || rotation == 2 ? object.sizeY : object.sizeX;

        Vector3d pos = world.position(x + sizeX / 2., y + sizeY / 2., plane);
        double centerZ = pos.z;
        pos.z = 0;

//        double wallWidth = 1;
//
//        if (type.baseType == LocationType.WALL_DECORATION) {
//            for (Location location : world.locations(x / 64, y / 64)) {
//                if (location.position.x != x || location.position.y != y && location.position.z == plane) {
//                    continue;
//                }
//
//                if (location.type != LocationType.WALL && location.type != LocationType.WALL_CORNER && location.type != LocationType.DIAGONAL_WALL) {
//                    continue;
//                }
//
//                if (location.object.decorationOffset == 16) {
//                    continue;
//                }
//
//                wallWidth = location.object.decorationOffset / 16.;
//                break;
//            }
//        }

        pos.add(object.offsetX * SCALE, object.offsetY * SCALE, -object.offsetZ * SCALE);
        int highlight = -1;

        double wallWidth = 0.25;
        Vector3d pos2 = new Vector3d();
        if (type == LocationType.WALL_DECORATION_DIAGONAL) {
            pos2.add(0.5 + wallWidth / 2, -(0.5 + wallWidth / 2), 0);
            pos2.add(0.5, 0.5, 0);
            flip = true;
        }

        if (type == LocationType.WALL_DECORATION_OPPOSITE) {
            pos2.add(wallWidth, 0, 0);
        }

        if (type == LocationType.WALL_DECORATION_OPPOSITE_DIAGONAL) {
            pos2.add(0.5 + wallWidth / 2, -(0.5 + wallWidth / 2), 0);
        }

        if (type == LocationType.WALL_DECORATION_DOUBLE) {
            return;
        }

        double extraPriority = 0;

        if (type.baseType == LocationType.WALL_DECORATION || type.baseType == LocationType.OBJECT || type == LocationType.FLOOR_DECORATION) {
            extraPriority += 5;
        }

        for (ModelDefinition model : models) {
            if (type == LocationType.WALL_CORNER) {
                model(object, highlight, plane, model, !flip, angle, scale, pos, centerZ, null, object.animation != null, extraPriority, pos2, object.mergeNormals);
                model(object, highlight, plane, model, flip, angle - Math.PI / 2, scale, pos, centerZ, null, object.animation != null, extraPriority, pos2, object.mergeNormals);
            } else {
                model(object, highlight, plane, model, flip, angle, scale, pos, centerZ, null, object.animation != null, extraPriority, pos2, object.mergeNormals);
            }
        }
    }

    public void model(ObjectDefinition object, int highlight, int plane, ModelDefinition model, boolean flip, double angle, Vector3d scale, Vector3d pos, double centerZ, TransformDefinition transform, boolean dynamic, double extraPriority, Vector3d pos2, boolean mergeNormals) {
        for (ModelDefinition.Face face : model.faces) {
            int color = object.colorSubstitutions.getOrDefault(face.color, face.color);

            if (face.transparency >= 254) {
                continue;
            }

            if (highlight != -1) {
                color = highlight;
            }

            if (face.texture != -1) {
                int texture = object.textureSubstitutions.getOrDefault(face.texture, face.texture);
                color = CacheSystem.getTextureDefinition(texture).averageColor;
            }

            Vector3d a = new Vector3d(face.a.x * SCALE, face.a.z * SCALE, -face.a.y * SCALE);
            Vector3d b = new Vector3d(face.b.x * SCALE, face.b.z * SCALE, -face.b.y * SCALE);
            Vector3d c = new Vector3d(face.c.x * SCALE, face.c.z * SCALE, -face.c.y * SCALE);

            if (flip) { // reverse vertex order for culling to work
                Vector3d t = a;
                a = c;
                c = t;
            }

            triangle(
                    adjustZ(plane, a.mul(1, flip ? -1 : 1, 1).mul(scale).add(pos2).rotateZ(angle).add(pos), centerZ, object),
                    adjustZ(plane, b.mul(1, flip ? -1 : 1, 1).mul(scale).add(pos2).rotateZ(angle).add(pos), centerZ, object),
                    adjustZ(plane, c.mul(1, flip ? -1 : 1, 1).mul(scale).add(pos2).rotateZ(angle).add(pos), centerZ, object),
                    color,
                    0xff - face.transparency,
                    model.priority + face.priority + extraPriority + plane * 20
            );
        }
    }

    public Vector3d adjustZ(int plane, Vector3d v, double centerZ, ObjectDefinition object) {
        if (object.contouredGround == -1) {
            v.z += centerZ;
        }

        if (object.contouredGround == 0) {
            v.z += world.height(v.x, v.y, plane);
        }

        if (object.contouredGround > 0) {
            throw new UnsupportedOperationException("???");
        }

        return v;
    }

    public void triangle(Vector3d a, Vector3d b, Vector3d c, int color, int alpha, double priority) {
        Vector3d normal = Util.normal(a, b, c);
        BufferBuilder buffer = alpha == 0xff ? opaqueBuffer : translucentBuffer;

        color &= 0xffffff;

        buffer.vertex(a, normal, alpha << 24 | color, priority);
        buffer.vertex(b, normal, alpha << 24 | color, priority);
        buffer.vertex(c, normal, alpha << 24 | color, priority);
    }

    public void vertex(Vector3d position, Vector3d normal, int color, int priority) {
        opaqueBuffer.vertex(position, normal, 0xff000000 | color, priority);
    }
}
