package my.first;

/**
 * Created by moyong on 16/6/12.
 */
public class HelloScala {
    public static void main(String[] args){
        Hello h = new Hello();
        h.sayHello( "scala" );

        Person p=new Person("摇摆少年梦", 27);
        System.out.println("name="+p.name()+" age="+p.age());
        //伴生对象的方法当做静态方法来使用
        System.out.println(Person.getIdentityNo());
    }
}
