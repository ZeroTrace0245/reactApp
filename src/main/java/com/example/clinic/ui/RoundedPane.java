package com.example.clinic.ui;

import javafx.geometry.Insets;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public class RoundedPane extends StackPane {
    public RoundedPane(double radius, Color fill) {
        setPadding(new Insets(24));
        setBackground(new Background(new BackgroundFill(fill, new CornerRadii(radius), Insets.EMPTY)));
        setEffect(new DropShadow(15, Color.rgb(0, 0, 0, 0.25)));
    }
}
