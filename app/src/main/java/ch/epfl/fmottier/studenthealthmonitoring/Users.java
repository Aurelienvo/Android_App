package ch.epfl.fmottier.studenthealthmonitoring;

/**
 * Created by Mottier on 14.11.2017.
 */

public class Users
{
    public String name;
    public String status;
    public String image;
    public String thumb_image;
    public String gender;
    public String birth_date;
    public String height;
    public String weight;

    public Users(String name, String status, String image, String thumb_image, String gender, String birth_date, String height, String weight)
    {
        this.name = name;
        this.status = status;
        this.image = image;
        this.thumb_image = thumb_image;
        this.gender = gender;
        this.birth_date = birth_date;
        this.height = height;
        this.weight = weight;
    }

    public Users()
    {

    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getImage()
    {
        return image;
    }

    public void setImage(String image)
    {
        this.image = image;
    }

    public String getThumbImage()
    {
        return thumb_image;
    }

    public void setThumbImage(String thumbImage)
    {
        this.thumb_image = thumb_image;
    }

    public String getGender()
    {
        return gender;
    }

    public void setGender(String gender)
    {
        this.gender = gender;
    }

    public String getBirthDate()
    {
        return birth_date;
    }

    public void setBirthDate(String birth_date)
    {
        this.birth_date = birth_date;
    }

    public String getHeight()
    {
        return height;
    }

    public void setHeight(String height)
    {
        this.height = height;
    }

    public String getWeight()
    {
        return weight;
    }

    public void setWeight(String weight)
    {
        this.weight = weight;
    }
}

