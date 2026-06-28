package com.guilherme.emobiliaria.receipt.ui.controller;

import com.guilherme.emobiliaria.receipt.domain.entity.PaymentProof;
import com.guilherme.emobiliaria.receipt.domain.entity.ProofFileType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

import java.util.List;

public class ProofSelectionDialog extends Dialog<PaymentProof> {

  public ProofSelectionDialog(List<PaymentProof> proofs, String title) {
    setTitle(title);
    setHeaderText(null);

    ListView<PaymentProof> listView = new ListView<>();
    listView.getItems().addAll(proofs);
    listView.setCellFactory(lv -> proofCell());
    listView.getSelectionModel().select(0);
    listView.setPrefHeight(Math.min(proofs.size() * 40.0 + 2, 200));

    getDialogPane().setContent(listView);
    getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

    setResultConverter(
        btn -> btn == ButtonType.OK ? listView.getSelectionModel().getSelectedItem() : null);
  }

  private static ListCell<PaymentProof> proofCell() {
    return new ListCell<>() {
      @Override
      protected void updateItem(PaymentProof proof, boolean empty) {
        super.updateItem(proof, empty);
        if (empty || proof == null) {
          setText(null);
        } else {
          String icon = proof.getFileType() == ProofFileType.PDF ? "📄 " : "🖼 ";
          setText(icon + proof.getOriginalFileName());
        }
      }
    };
  }
}
