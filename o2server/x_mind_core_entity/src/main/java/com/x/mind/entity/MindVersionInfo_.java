/** 
 *  Generated by OpenJPA MetaModel Generator Tool.
**/

package com.x.mind.entity;

import com.x.base.core.entity.SliceJpaObject_;
import java.lang.Boolean;
import java.lang.Integer;
import java.lang.String;
import javax.persistence.metamodel.SingularAttribute;

@javax.persistence.metamodel.StaticMetamodel
(value=com.x.mind.entity.MindVersionInfo.class)
@javax.annotation.Generated
(value="org.apache.openjpa.persistence.meta.AnnotationProcessor6",date="Sun Dec 23 11:59:30 CST 2018")
public class MindVersionInfo_ extends SliceJpaObject_  {
    public static volatile SingularAttribute<MindVersionInfo,String> creator;
    public static volatile SingularAttribute<MindVersionInfo,String> creatorUnit;
    public static volatile SingularAttribute<MindVersionInfo,String> description;
    public static volatile SingularAttribute<MindVersionInfo,Integer> fileVersion;
    public static volatile SingularAttribute<MindVersionInfo,String> folderId;
    public static volatile SingularAttribute<MindVersionInfo,String> id;
    public static volatile SingularAttribute<MindVersionInfo,String> mindId;
    public static volatile SingularAttribute<MindVersionInfo,String> name;
    public static volatile SingularAttribute<MindVersionInfo,Boolean> shared;
}
