/** 
 *  Generated by OpenJPA MetaModel Generator Tool.
**/

package com.x.file.core.entity.open;

import com.x.base.core.entity.StorageObject_;
import java.lang.Long;
import java.lang.String;
import java.util.Date;
import javax.persistence.metamodel.SingularAttribute;

@javax.persistence.metamodel.StaticMetamodel
(value=com.x.file.core.entity.open.File.class)
@javax.annotation.Generated
(value="org.apache.openjpa.persistence.meta.AnnotationProcessor6",date="Sun Dec 23 11:59:08 CST 2018")
public class File_ extends StorageObject_  {
    public static volatile SingularAttribute<File,String> extension;
    public static volatile SingularAttribute<File,String> id;
    public static volatile SingularAttribute<File,Date> lastNotExistedTime;
    public static volatile SingularAttribute<File,Date> lastUpdateTime;
    public static volatile SingularAttribute<File,Long> length;
    public static volatile SingularAttribute<File,String> name;
    public static volatile SingularAttribute<File,String> person;
    public static volatile SingularAttribute<File,String> reference;
    public static volatile SingularAttribute<File,ReferenceType> referenceType;
    public static volatile SingularAttribute<File,String> storage;
}
