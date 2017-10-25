/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.generator;

public class VOPropertyDescription {
    private String name;
    private String genericType;
    private String type;
    private String typeParameter;
    private String typeParameterWithoutPackage;
    private boolean enumType;

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param genericType
     *            the genericType to set
     */
    public void setGenericType(String genericType) {
        this.genericType = genericType;
    }

    /**
     * @return the genericType
     */
    public String getGenericType() {
        return genericType;
    }

    /**
     * @param type
     *            the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param typeParameter
     *            the typeParameter to set
     */
    public void setTypeParameter(String typeParameter) {
        this.typeParameter = typeParameter;
    }

    /**
     * @return the typeParameter
     */
    public String getTypeParameter() {
        return typeParameter;
    }

    public String toString() {
        StringBuffer str = new StringBuffer();

        str.append("Name = " + name + ";");
        str.append(" GenericType = " + genericType + ";");
        str.append(" Type = " + type + ";");
        str.append(" TypeParameter = " + typeParameter + ";");
        str.append(" TypeParameterWithoutPackage = "
                + typeParameterWithoutPackage);

        return str.toString();
    }

    /**
     * @param typeParameterWithoutPackage
     *            the typeParameterWithoutPackage to set
     */
    public void setTypeParameterWithoutPackage(
            String typeParameterWithoutPackage) {
        this.typeParameterWithoutPackage = typeParameterWithoutPackage;
    }

    /**
     * @return the typeParameterWithoutPackage
     */
    public String getTypeParameterWithoutPackage() {
        return typeParameterWithoutPackage;
    }

    /**
     * @param enumType
     *            the enumType to set
     */
    public void setEnumType(boolean enumType) {
        this.enumType = enumType;
    }

    /**
     * @return the enumType
     */
    public boolean isEnumType() {
        return enumType;
    }

}
