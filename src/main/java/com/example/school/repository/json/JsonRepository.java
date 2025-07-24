package com.example.school.repository.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class JsonRepository<T> {

    private final ObjectMapper mapper;
    private final File file;
    private final Class<T> type;
    protected List<T> cachedData = new ArrayList<>();

    public JsonRepository(String filename, Class<T> type) {
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule()); // Register JavaTime module for LocalDate
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // Write LocalDate as ISO

        this.file = new File("data/" + filename);
        this.type = type;

        try {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
                mapper.writeValue(file, cachedData);
            } else {
                CollectionType listType = mapper.getTypeFactory().constructCollectionType(List.class, type);
                cachedData = mapper.readValue(file, listType);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize JSON file", e);
        }
    }

    public List<T> findAll() {
        return new ArrayList<>(cachedData);
    }

    public T save(T item) {
        try {
            Long newId = null;

            try {
                newId = (Long) item.getClass().getMethod("getId").invoke(item);
            } catch (NoSuchMethodException ignored) {
            }

            if (newId == null) {
                long maxId = cachedData.stream()
                        .map(existingItem -> {
                            try {
                                return (Long) existingItem.getClass().getMethod("getId").invoke(existingItem);
                            } catch (Exception e) {
                                return 0L;
                            }
                        })
                        .max(Long::compareTo)
                        .orElse(0L);
                item.getClass().getMethod("setId", Long.class).invoke(item, maxId + 1);
            } else {
                Long finalNewId = newId;
                cachedData.removeIf(existingItem -> {
                    try {
                        Long id = (Long) existingItem.getClass().getMethod("getId").invoke(existingItem);
                        return id.equals(finalNewId);
                    } catch (Exception e) {
                        return false;
                    }
                });
            }

            cachedData.add(item);
            saveToFile();
            return item;

        } catch (Exception e) {
            throw new RuntimeException("Failed to save item", e);
        }
    }

    public void saveAll(List<T> items) {
        try {
            this.cachedData = new ArrayList<>(items);
            saveToFile();
        } catch (Exception e) {
            throw new RuntimeException("Failed to save all items", e);
        }
    }

    public void saveToFile() {
        try {
            System.out.println("Writing " + cachedData.size() + " items to file: " + file.getName());
            mapper.writeValue(file, cachedData);
            System.out.println("Saved successfully.");
        } catch (Exception e) {
            throw new RuntimeException("Failed to write JSON to file", e);
        }
    }

    public T findById(Long id) {
        return cachedData.stream()
                .filter(item -> {
                    try {
                        Long value = (Long) item.getClass().getMethod("getId").invoke(item);
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

    public void deleteById(Long id) {
        cachedData.removeIf(item -> {
            try {
                Long value = (Long) item.getClass().getMethod("getId").invoke(item);
                return value.equals(id);
            } catch (Exception e) {
                return false;
            }
        });
        saveToFile();
    }

    public void clear() {
        cachedData.clear();
        saveToFile();
    }
}