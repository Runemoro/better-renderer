package renderer.renderer;

import org.joml.Vector3d;
import renderer.gl.VertexBuffer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class BufferBuilder {
    public static final int VERTEX_SIZE = 32;
    private ByteBuffer buffer;
    public int vertexCount = 0;
    private int memoryUsage = -1;
    private VertexBuffer uploaded;

    public BufferBuilder(int triangles) {
        buffer = ByteBuffer.allocateDirect(triangles * 3 * VERTEX_SIZE).order(ByteOrder.nativeOrder());
    }

    public void vertex(Vector3d position, Vector3d normal, int color, double priority) {
        if (buffer.limit() - buffer.position() < VERTEX_SIZE) {
            buffer.limit(buffer.position());
            buffer.position(0);
            ByteBuffer newBuffer = ByteBuffer.allocateDirect(buffer.capacity() * 2).order(ByteOrder.nativeOrder());
            newBuffer.put(buffer);
            buffer = newBuffer;
        }

        vertexCount++;
        buffer.putFloat((float) position.x);
        buffer.putFloat((float) position.y);
        buffer.putFloat((float) position.z);

        buffer.putFloat((float) normal.x);
        buffer.putFloat((float) normal.y);
        buffer.putFloat((float) normal.z);

        buffer.putInt(color);
        buffer.putFloat((float) priority);
    }

    public VertexBuffer buffer() {
        if (uploaded == null) {
            memoryUsage = buffer.position();
            uploaded = new VertexBuffer();
            buffer.limit(buffer.position());
            uploaded.set(vertexCount, (ByteBuffer) buffer.position(0));
            buffer = null;
        }

        return uploaded;
    }

    public void close() {
//        buffer = null;

        if (uploaded != null) {
            uploaded.close();
        }
    }

    public int memoryUsage() {
        return buffer != null ? buffer.limit() : memoryUsage;
    }
}
