package renderer.cache;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
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

public class GameCache {
    public static final int REVISION = 192;
    public static final int ARCHIVE_COUNT = 21;
    private final Int2ObjectMap<Archive> archives = new FastIntMap<>();

    public void init() throws IOException {
        Socket socket = new Socket("oldschool2.runescape.com", 43594);
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

    public void initLocal() {
        for (int id = 0; id < ARCHIVE_COUNT; id++) {
            Archive archive = new Archive(id);
            archives.put(id, archive);
            archive.load(0, 0);
        }

        decrypt();
    }

    private void decrypt() {
        try {
            Type type = new TypeToken<Map<Integer, int[]>>() {
            }.getType();
            Map<Integer, int[]> keys = new Gson().fromJson(Files.newBufferedReader(Paths.get("xtea.json")), type);

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
}
