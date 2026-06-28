package com.guilherme.emobiliaria.receipt.ui.controller;

import com.guilherme.emobiliaria.receipt.domain.entity.PaymentProof;
import com.guilherme.emobiliaria.receipt.domain.entity.ProofFileType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ListCell;

import java.util.List;

public class ProofSelectionDialog extends ChoiceDialog<PaymentProof> {

  public ProofSelectionDialog(List<PaymentProof> proofs, String title) {
    super(proofs.get(0), proofs);
    setTitle(title);
    setHeaderText(null);
    getDialogPane().lookupButton(javafx.scene.control.ButtonType.OK);

    setResultConverter(btn -> {
      if (btn == javafx.scene.control.ButtonType.OK) {
        return getSelectedItem();
      }
      return null;
    });
  }

  public static ListCell<PaymentProof> proofCell() {
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
