package ecologylab.xml;

public class StringState extends ElementState
{
    @xml_attribute protected String string;
    
    public StringState()
    {
        super();
    }
    
    public StringState(String string)
    {
        this.string = string;
    }

    /**
     * @return the string
     */
    public String getString()
    {
        return string;
    }
}
