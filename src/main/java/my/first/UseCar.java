package my.first;

/**
 * Created by moyong on 16/6/12.
 */
public class UseCar {

    public static void main(String[] args) {
        Car car = new Car(2009);

        System.out.println(car);
        car.drive(10);
        System.out.println(car);
    }

}
