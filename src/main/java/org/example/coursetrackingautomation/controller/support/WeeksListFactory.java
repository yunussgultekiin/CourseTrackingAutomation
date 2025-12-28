package org.example.coursetrackingautomation.controller.support;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public final class WeeksListFactory {

    private WeeksListFactory() {
    }

    public static ObservableList<String> buildWeeks(int startInclusive, int endInclusive) {
        ObservableList<String> weeks = FXCollections.observableArrayList();
        for (int i = startInclusive; i <= endInclusive; i++) {
            weeks.add(String.valueOf(i));
        }
        return weeks;
    }
}
