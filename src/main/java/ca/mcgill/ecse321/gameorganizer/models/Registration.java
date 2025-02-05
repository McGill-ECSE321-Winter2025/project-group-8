package ca.mcgill.ecse321.gameorganizer.models;

import java.util.Date;

public class Registration
{



    private static int nextId = 1;



    //Registration Attributes
    private Date registrationDate;


    private int id;

    //Registration Associations
    private Event forEvent;
    private Account registeredBy;



    public Registration(Date aRegistrationDate, Event aForEvent, Account aRegisteredBy)
    {
        registrationDate = aRegistrationDate;
        id = nextId++;
        if (!setForEvent(aForEvent))
        {
            throw new RuntimeException("Unable to create Registration due to aForEvent. See https://manual.umple.org?RE002ViolationofAssociationMultiplicity.html");
        }
        if (!setRegisteredBy(aRegisteredBy))
        {
            throw new RuntimeException("Unable to create Registration due to aRegisteredBy. See https://manual.umple.org?RE002ViolationofAssociationMultiplicity.html");
        }
    }



    public boolean setRegistrationDate(Date aRegistrationDate)
    {
        boolean wasSet = false;
        registrationDate = aRegistrationDate;
        wasSet = true;
        return wasSet;
    }

    public Date getRegistrationDate()
    {
        return registrationDate;
    }

    public int getId()
    {
        return id;
    }

    public Event getForEvent()
    {
        return forEvent;
    }

    public Account getRegisteredBy()
    {
        return registeredBy;
    }

    public boolean setForEvent(Event aNewForEvent)
    {
        boolean wasSet = false;
        if (aNewForEvent != null)
        {
            forEvent = aNewForEvent;
            wasSet = true;
        }
        return wasSet;
    }

    public boolean setRegisteredBy(Account aNewRegisteredBy)
    {
        boolean wasSet = false;
        if (aNewRegisteredBy != null)
        {
            registeredBy = aNewRegisteredBy;
            wasSet = true;
        }
        return wasSet;
    }

    public void delete()
    {
        forEvent = null;
        registeredBy = null;
    }




    public String toString()
    {
        return super.toString() + "["+
                "id" + ":" + getId()+ "]" + System.getProperties().getProperty("line.separator") +
                "  " + "registrationDate" + "=" + (getRegistrationDate() != null ? !getRegistrationDate().equals(this)  ? getRegistrationDate().toString().replaceAll("  ","    ") : "this" : "null") + System.getProperties().getProperty("line.separator") +
                "  " + "forEvent = "+(getForEvent()!=null?Integer.toHexString(System.identityHashCode(getForEvent())):"null") + System.getProperties().getProperty("line.separator") +
                "  " + "registeredBy = "+(getRegisteredBy()!=null?Integer.toHexString(System.identityHashCode(getRegisteredBy())):"null");
    }
}
