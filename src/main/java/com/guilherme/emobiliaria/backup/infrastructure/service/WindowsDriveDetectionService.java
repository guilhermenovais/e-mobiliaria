package com.guilherme.emobiliaria.backup.infrastructure.service;

import com.guilherme.emobiliaria.backup.domain.entity.RemovableDrive;
import com.guilherme.emobiliaria.backup.domain.service.DriveDetectionService;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WindowsDriveDetectionService implements DriveDetectionService {

  private static final Logger log = LoggerFactory.getLogger(WindowsDriveDetectionService.class);

  @Inject
  public WindowsDriveDetectionService() {
  }

  @Override
  public List<RemovableDrive> detectRemovableDrives() {
    File[] roots = File.listRoots();
    if (roots == null) {
      return List.of();
    }

    FileSystemView fsv = FileSystemView.getFileSystemView();
    List<RemovableDrive> drives = new ArrayList<>();

    for (File root : roots) {
      try {
        String typeDescription = fsv.getSystemTypeDescription(root);
        if (typeDescription != null && typeDescription.toLowerCase(Locale.ROOT).contains("usb")) {
          String label = fsv.getSystemDisplayName(root);
          if (label == null || label.isBlank()) {
            label = root.getAbsolutePath();
          }
          drives.add(new RemovableDrive(label, root.toPath()));
        }
      } catch (Exception e) {
        log.warn("Failed to check drive type for {}: {}", root, e.getMessage());
      }
    }

    return List.copyOf(drives);
  }
}
