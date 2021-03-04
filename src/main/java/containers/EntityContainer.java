package main.java.containers;

public class EntityContainer
{
    private String entityVal;
    private String entityId;
    private int existCount = 1;

    public EntityContainer(String ent, String entityId)
    {
        this.entityVal=ent;
        this.entityId=entityId;
    }

    public String getEntityVal()
    {
        return entityVal;
    }
    public String getEntityId()
    {
        return entityId;
    }
    public int getCount()   { return existCount; }
    public void setCount(int value) { existCount = value;}
}
