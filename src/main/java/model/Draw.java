package model;
public class Draw {
    private String action;
    private double x;
    private double y;
    private String type;
    
    public Draw(){
        
    }
    
    public Draw(String action, double x, double y,  String type){
        this.action = action;
        this.x = x;
        this.y = y;
        this.type = type;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    
    
}
