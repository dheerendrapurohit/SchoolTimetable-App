package com.example.school.repository.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public abstract class JsonRepository<T> {

    private final ObjectMapper mapper;
    private final File file;
    private final Class<T> type;
    protected List<T> cachedData = new ArrayList<>();

    private final Method getIdMethod;
    private final Method setIdMethod;

    public JsonRepository(String filename, Class<T> type) {
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        this.file = new File("data/" + filename);
        this.type = type;

        try {
            this.getIdMethod = type.getMethod("getId");
            this.setIdMethod = type.getMethod("setId", Long.class);
        } catch (Exception e) {
            throw new RuntimeException("Model class must have getId() and setId(Long) methods", e);
        }

        try {
            file.getParentFile().mkdirs(); // Ensure data folder exists
            if (!file.exists()) {
                file.createNewFile();
                System.out.println("📄 Created new file: " + file.getName());
                // Don't save empty array immediately — just keep empty in-memory list
                cachedData = new ArrayList<>();
            } else {
                try {
                    CollectionType listType = mapper.getTypeFactory().constructCollectionType(List.class, type);
                    cachedData = mapper.readValue(file, listType);
                    System.out.println("✅ Loaded " + cachedData.size() + " items from " + file.getName());
                } catch (Exception readEx) {
                    System.err.println("⚠ Warning: Failed to read JSON file " + file.getName() + ". Replacing with empty list.");
                    readEx.printStackTrace();
                    cachedData = new ArrayList<>();
                    saveToFile(); // Overwrite only if file is corrupted
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to initialize JSON file: " + file.getName(), e);
        }
    }

    public List<T> findAll() {
        return new ArrayList<>(cachedData);
    }

    public T findById(Long id) {
        return cachedData.stream()
                .filter(item -> {
                    try {
                        Long value = (Long) getIdMethod.invoke(item);
                        return value.equals(id);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .findFirst()
                .orElse(null);
    }

    public boolean existsById(Long id) {
        return findById(id) != null;
    }

    public T save(T item) {
        try {
            Long id = (Long) getIdMethod.invoke(item);

            if (id == null) {
                long maxId = cachedData.stream()
                        .map(existingItem -> {
                            try {
                                return (Long) getIdMethod.invoke(existingItem);
                            } catch (Exception e) {
                                return 0L;
                            }
                        })
                        .max(Long::compareTo)
                        .orElse(0L);
                setIdMethod.invoke(item, maxId + 1);
            } else {
                cachedData.removeIf(existingItem -> {
                    try {
                        Long existingId = (Long) getIdMethod.invoke(existingItem);
                        return existingId.equals(id);
                    } catch (Exception e) {
                        return false;
                    }
                });
            }

            cachedData.add(item);
            saveToFile();
            return item;

        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to save item", e);
        }
    }

    public void saveAll(List<T> items) {
        try {
            this.cachedData = new ArrayList<>(items);
            saveToFile();
        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to save all items", e);
        }
    }

    public void deleteById(Long id) {
        cachedData.removeIf(item -> {
            try {
                Long value = (Long) getIdMethod.invoke(item);
                return value.equals(id);
            } catch (Exception e) {
                return false;
            }
        });
        saveToFile();
    }

    public void clear() {
        System.out.println("⚠ Clearing all data from " + file.getName());
        cachedData.clear();
        saveToFile();
    }

    public void saveToFile() {
        try {
            System.out.println("💾 Writing " + cachedData.size() + " items to " + file.getName());
            mapper.writeValue(file, cachedData);
            System.out.println("✅ Save successful.");
        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to write JSON to file", e);
        }
    }

}
