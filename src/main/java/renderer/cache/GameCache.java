package renderer.cache;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.runelite.client.RuneLite;
import renderer.util.FastIntMap;
import renderer.util.NetworkBuffer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;

public class GameCache {
    public static final int REVISION = 192;
    public static final int ARCHIVE_COUNT = 21;
    public static final int[] WORLDS = {
            306, 307, 313, 315, 319, 320, 323, 324, 331, 332, 338, 339, 340, 347, 348, 355, 356, 357, 374, 378, 418, 419, 420,
            421, 422, 428, 429, 430, 431, 432, 433, 434, 435, 436, 437, 438, 439, 440, 443, 444, 445, 446, 301, 305, 314, 321,
            322, 329, 330, 337, 345, 346, 353, 354, 361, 362, 369, 370, 377, 385, 386, 393, 394, 400, 415, 416, 417, 467, 468,
            469, 470, 471, 472, 473, 474, 475, 476, 477, 491, 492, 493, 494, 495, 496, 401, 402, 403, 404, 409, 410, 411
    };

    private final Int2ObjectMap<Archive> archives = new FastIntMap<>();

    public void init() throws IOException {
        Socket socket = new Socket(getWorld(), 43594);
        OutputStream out = socket.getOutputStream();
        InputStream in = socket.getInputStream();
        socket.setSoTimeout(1000000000);

        NetworkBuffer buffer = new NetworkBuffer(5);
        buffer.writeByte(15);
        buffer.writeInt(REVISION);
        out.write(buffer.array, 0, buffer.offset);

        int read = in.read();
        if (read != 0) {
            throw new IllegalStateException("" + read);
        }

        for (int id = 0; id < 21; id++) {
            archives.put(id, new Archive(id));
        }

        Js5.request(255, 255, true, data -> {
            NetworkBuffer buf = new NetworkBuffer(data);

            for (int id = 0; id < 21; id++) {
                buf.offset = id * 8 + 5;
                archive(id).load(buf.readInt(), buf.readInt());
            }
        });

        while (!Js5.tick(out, in)) ;
        decrypt();
    }

    private void decrypt() {
        try {
            Type type = new TypeToken<Map<Integer, int[]>>() {
            }.getType();
            Map<Integer, int[]> keys = new Gson().fromJson(Files.newBufferedReader(RuneLite.RUNELITE_DIR.toPath().resolve("better-renderer/xtea.json")), type);

            for (Map.Entry<Integer, int[]> entry : keys.entrySet()) {
                int region = entry.getKey();
                int[] key = entry.getValue();

                int regionX = (region >> 8) & 0xff;
                int regionY = region & 0xff;

                try {
                    archive(5).group("l" + regionX + "_" + regionY).buildFiles(key);
                } catch (Exception e) {
                    System.err.println("Region (" + regionX + ", " + regionY + ") could not be decrypted with key " + Arrays.toString(key));
                }
            }
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    public Archive archive(int id) {
        return archives.get(id);
    }

    public byte[] get(int archive, int group, int file) {
        return archive(archive).group(group).file(file);
    }

    private static String getWorld() {
        return "oldschool" + (WORLDS[new Random().nextInt(WORLDS.length)] - 300) + ".runescape.com";
    }
}
