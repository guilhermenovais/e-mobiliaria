module com.guilherme.emobiliaria {
  requires javafx.controls;
  requires javafx.fxml;


  opens com.guilherme.emobiliaria to javafx.fxml;
  exports com.guilherme.emobiliaria;
}
