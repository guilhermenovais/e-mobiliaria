package com.guilherme.emobiliaria.shared.exception;

public class ExportFolderNotWritableException extends BusinessException {

  public ExportFolderNotWritableException() {
    super(ErrorMessage.Receipt.EXPORT_FOLDER_NOT_WRITABLE);
  }
}
