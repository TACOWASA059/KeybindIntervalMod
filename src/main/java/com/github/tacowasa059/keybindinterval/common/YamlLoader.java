package com.github.tacowasa059.keybindinterval.common;

import com.github.tacowasa059.keybindinterval.KeybindInterval;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.fml.loading.FMLPaths;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class YamlLoader {
    private static Map<String, Map<String, Integer>> processedData = new ConcurrentHashMap<>();

    private static final String CONFIG_FILE_NAME = KeybindInterval.MODID +".yml";
    private static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve(CONFIG_FILE_NAME);

    public static void loadYaml(MinecraftServer server) {
        try {

            // `config/settingslocker.yml` が存在しない場合、デフォルトリソースからコピー
            if (!Files.exists(CONFIG_PATH)) {
                copyDefaultConfig(server);
            }


            InputStream inputStream = Files.newInputStream(CONFIG_PATH, StandardOpenOption.READ);
            InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            Yaml yaml = new Yaml();
            Map<String, Object> data = yaml.load(reader);

            processedData = data.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> ((Map<String, Object>) entry.getValue()).entrySet().stream()
                                    .collect(Collectors.toMap(
                                            Map.Entry::getKey,
                                            e -> {
                                                try {
                                                    return Integer.valueOf(String.valueOf(e.getValue()));
                                                } catch (NumberFormatException ex) {
                                                    return -1;
                                                }
                                            }
                                    ))
                    ));

            reader.close();
            inputStream.close();



        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveToYaml() {
        // YAMLのフォーマットオプションを設定
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setIndent(2);
        options.setPrettyFlow(true);

        Yaml yaml = new Yaml(options);
        String yamlContent = yaml.dump(processedData);

        // コメントを追加
        StringBuilder yamlWithComments = new StringBuilder();
        yamlWithComments.append("# keybinding\n");
        yamlWithComments.append("# interval: Usage time interval (tick)\n");
        yamlWithComments.append("# max_time: Continuous usage time (tick)\n");
        yamlWithComments.append("# if interval == -1 -> skip\n");
        yamlWithComments.append("# if max_time <=  1 -> 1tick\n");
        yamlWithComments.append(yamlContent);

        try (FileWriter writer = new FileWriter(CONFIG_PATH.toFile(), StandardCharsets.UTF_8)) {
            writer.write(yamlWithComments.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static void copyDefaultConfig(MinecraftServer server) {
        try {
            ResourceLocation resourceLocation = new ResourceLocation(KeybindInterval.MODID, "keybindinterval.yml");
            ResourceManager resourceManager = server.getResourceManager();
            Resource resource = resourceManager.getResource(resourceLocation).orElse(null);

            if (resource != null) {
                try{
                    Files.createDirectories(CONFIG_PATH.getParent());
                }catch (IOException ignored){
                    System.out.println("Making config directory failed.");
                }

                Files.copy(resource.open(), CONFIG_PATH, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Default config copied to: " + CONFIG_PATH);
            } else {
                System.err.println("Default keybindinterval.yml not found in mod resources.");
            }
        } catch (IOException e) {
            System.err.println("Failed to copy default config: " + e.getMessage());
        }
    }

    public static boolean contains(String key){
        if(processedData==null) return false;
        return processedData.containsKey(key);
    }
    public static Set<String> getKeys() {
        if(processedData==null) return new HashSet<>();
        return processedData.keySet();
    }

    public static Map<String, Integer> getOrDefault(String key, Map<String, Integer> mp){
        if(processedData==null) return mp;
        Map<String, Integer> data = processedData.get(key);
        if(data==null) return mp;
        return data;
    }

    public static Map<String, Map<String, Integer>> get(){
        if(processedData==null) return new ConcurrentHashMap<>();
        return processedData;
    }


    public static void setData(Map<String,Map<String, Integer>> newData) {
        processedData = newData;
    }

    public static void reset(){
        processedData = null;
    }

    public static void addData(String path, String category, Integer value) {
        processedData.computeIfAbsent(path, k -> new HashMap<>());

        Map<String, Integer> map = processedData.get(path);

        map.put(category, value);
    }

}
