package ca.mcgill.ecse321.gameorganizer.model;

import java.util.Date;

public class Game
{



    private static int nextId = 1;




    private String name;
    private int minPlayers;
    private int maxPlayers;
    private String image;
    private Date dateAdded;


    private int id;


    private GameOwner owner;



    public Game(String aName, int aMinPlayers, int aMaxPlayers, String aImage, Date aDateAdded, GameOwner aOwner)
    {
        name = aName;
        minPlayers = aMinPlayers;
        maxPlayers = aMaxPlayers;
        image = aImage;
        dateAdded = aDateAdded;
        id = nextId++;
        if (!setOwner(aOwner))
        {
            throw new RuntimeException("Unable to create Game due to aOwner. See https://manual.umple.org?RE002ViolationofAssociationMultiplicity.html");
        }
    }



    public boolean setName(String aName)
    {
        boolean wasSet = false;
        name = aName;
        wasSet = true;
        return wasSet;
    }

    public boolean setMinPlayers(int aMinPlayers)
    {
        boolean wasSet = false;
        minPlayers = aMinPlayers;
        wasSet = true;
        return wasSet;
    }

    public boolean setMaxPlayers(int aMaxPlayers)
    {
        boolean wasSet = false;
        maxPlayers = aMaxPlayers;
        wasSet = true;
        return wasSet;
    }

    public boolean setImage(String aImage)
    {
        boolean wasSet = false;
        image = aImage;
        wasSet = true;
        return wasSet;
    }

    public boolean setDateAdded(Date aDateAdded)
    {
        boolean wasSet = false;
        dateAdded = aDateAdded;
        wasSet = true;
        return wasSet;
    }

    public String getName()
    {
        return name;
    }

    public int getMinPlayers()
    {
        return minPlayers;
    }

    public int getMaxPlayers()
    {
        return maxPlayers;
    }

    public String getImage()
    {
        return image;
    }

    public Date getDateAdded()
    {
        return dateAdded;
    }

    public int getId()
    {
        return id;
    }

    public GameOwner getOwner()
    {
        return owner;
    }

    public boolean setOwner(GameOwner aNewOwner)
    {
        boolean wasSet = false;
        if (aNewOwner != null)
        {
            owner = aNewOwner;
            wasSet = true;
        }
        return wasSet;
    }

    public void delete()
    {
        owner = null;
    }



    public String toString()
    {
        return super.toString() + "["+
                "id" + ":" + getId()+ "," +
                "name" + ":" + getName()+ "," +
                "minPlayers" + ":" + getMinPlayers()+ "," +
                "maxPlayers" + ":" + getMaxPlayers()+ "," +
                "image" + ":" + getImage()+ "]" + System.getProperties().getProperty("line.separator") +
                "  " + "dateAdded" + "=" + (getDateAdded() != null ? !getDateAdded().equals(this)  ? getDateAdded().toString().replaceAll("  ","    ") : "this" : "null") + System.getProperties().getProperty("line.separator") +
                "  " + "owner = "+(getOwner()!=null?Integer.toHexString(System.identityHashCode(getOwner())):"null");
    }
}
