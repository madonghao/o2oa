/** 
 *  Generated by OpenJPA MetaModel Generator Tool.
**/

package com.x.meeting.core.entity;

import com.x.base.core.entity.SliceJpaObject_;
import java.lang.Boolean;
import java.lang.Integer;
import java.lang.String;
import javax.persistence.metamodel.SingularAttribute;

@javax.persistence.metamodel.StaticMetamodel
(value=com.x.meeting.core.entity.Room.class)
@javax.annotation.Generated
(value="org.apache.openjpa.persistence.meta.AnnotationProcessor6",date="Sun Dec 23 11:59:21 CST 2018")
public class Room_ extends SliceJpaObject_  {
    public static volatile SingularAttribute<Room,String> auditor;
    public static volatile SingularAttribute<Room,Boolean> available;
    public static volatile SingularAttribute<Room,String> building;
    public static volatile SingularAttribute<Room,Integer> capacity;
    public static volatile SingularAttribute<Room,String> device;
    public static volatile SingularAttribute<Room,Integer> floor;
    public static volatile SingularAttribute<Room,String> id;
    public static volatile SingularAttribute<Room,String> name;
    public static volatile SingularAttribute<Room,String> phoneNumber;
    public static volatile SingularAttribute<Room,String> photo;
    public static volatile SingularAttribute<Room,String> pinyin;
    public static volatile SingularAttribute<Room,String> pinyinInitial;
    public static volatile SingularAttribute<Room,String> roomNumber;
}
