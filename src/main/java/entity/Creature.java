package entity;

public abstract class Creature extends Entity {


    private int age;

    public Creature(String name, int age) {
        super(name);
        if(age > 0){
            this.age = age;
        }else {
            throw new IllegalArgumentException("Age can`t be negative ");
        }

    }

    public void growOlder(){
        this.age = this.age + 1;
    }

}
