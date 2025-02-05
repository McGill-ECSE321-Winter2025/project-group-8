package ca.mcgill.ecse321.gameorganizer.model;

public class Account
{



    private static int nextId = 1;




    private String name;
    private String email;
    private String password;


    private int id;


    public Account(String aName, String aEmail, String aPassword)
    {
        name = aName;
        email = aEmail;
        password = aPassword;
        id = nextId++;
    }



    public boolean setName(String aName)
    {
        boolean wasSet = false;
        name = aName;
        wasSet = true;
        return wasSet;
    }

    public boolean setEmail(String aEmail)
    {
        boolean wasSet = false;
        email = aEmail;
        wasSet = true;
        return wasSet;
    }

    public boolean setPassword(String aPassword)
    {
        boolean wasSet = false;
        password = aPassword;
        wasSet = true;
        return wasSet;
    }

    public String getName()
    {
        return name;
    }

    public String getEmail()
    {
        return email;
    }

    public String getPassword()
    {
        return password;
    }

    public int getId()
    {
        return id;
    }

    public void delete()
    {}


    public String toString()
    {
        return super.toString() + "["+
                "id" + ":" + getId()+ "," +
                "name" + ":" + getName()+ "," +
                "email" + ":" + getEmail()+ "," +
                "password" + ":" + getPassword()+ "]";
    }
}
