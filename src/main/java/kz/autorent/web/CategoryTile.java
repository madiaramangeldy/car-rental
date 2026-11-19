package kz.autorent.web;

import kz.autorent.model.Car;
import kz.autorent.model.CarCategory;

/** Басты беттегі санат плиткасы: санат, оның өкіл-көлігі (фото үшін) және көліктер саны. */
public class CategoryTile {

    private final CarCategory category;
    private final Car car;
    private final int count;

    public CategoryTile(CarCategory category, Car car, int count) {
        this.category = category;
        this.car = car;
        this.count = count;
    }

    public CarCategory getCategory() { return category; }
    public Car getCar() { return car; }
    public int getCount() { return count; }
}
