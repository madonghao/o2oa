/** 
 *  Generated by OpenJPA MetaModel Generator Tool.
**/

package com.x.organization.core.entity;

import com.x.base.core.entity.SliceJpaObject_;
import java.lang.Integer;
import java.lang.String;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;

@javax.persistence.metamodel.StaticMetamodel
(value=com.x.organization.core.entity.Group.class)
@javax.annotation.Generated
(value="org.apache.openjpa.persistence.meta.AnnotationProcessor6",date="Sun Dec 23 11:59:45 CST 2018")
public class Group_ extends SliceJpaObject_  {
    public static volatile SingularAttribute<Group,String> description;
    public static volatile SingularAttribute<Group,String> distinguishedName;
    public static volatile ListAttribute<Group,String> groupList;
    public static volatile SingularAttribute<Group,String> id;
    public static volatile SingularAttribute<Group,String> name;
    public static volatile SingularAttribute<Group,Integer> orderNumber;
    public static volatile ListAttribute<Group,String> personList;
    public static volatile SingularAttribute<Group,String> pinyin;
    public static volatile SingularAttribute<Group,String> pinyinInitial;
    public static volatile SingularAttribute<Group,String> unique;
    public static volatile ListAttribute<Group,String> unitList;
}
