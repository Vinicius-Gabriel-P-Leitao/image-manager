package me.image.manager.google.drive.dto;

public record DriveFolder(String id, String name) {
    @Override
    public String toString() {
        return name;
    }
}
