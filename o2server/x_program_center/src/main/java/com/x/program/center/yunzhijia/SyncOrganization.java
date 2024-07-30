package com.x.program.center.yunzhijia;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.google.gson.Gson;
import com.x.base.core.entity.JpaObject;
import com.x.base.core.project.annotation.FieldDescribe;
import com.x.base.core.project.bean.WrapCopier;
import com.x.base.core.project.bean.WrapCopierFactory;
import com.x.base.core.project.http.EffectivePerson;
import com.x.organization.core.entity.*;
import com.x.organization.core.entity.enums.PersonStatusEnum;
import com.x.program.center.jaxrs.yunzhijia.BaseAction;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.graalvm.polyglot.Source;

import com.x.base.core.container.EntityManagerContainer;
import com.x.base.core.entity.annotation.CheckPersistType;
import com.x.base.core.entity.annotation.CheckRemoveType;
import com.x.base.core.entity.type.GenderType;
import com.x.base.core.project.cache.CacheManager;
import com.x.base.core.project.config.Config;
import com.x.base.core.project.gson.GsonPropertyObject;
import com.x.base.core.project.gson.XGsonBuilder;
import com.x.base.core.project.logger.Logger;
import com.x.base.core.project.logger.LoggerFactory;
import com.x.base.core.project.scripting.GraalvmScriptingFactory;
import com.x.base.core.project.tools.ListTools;
import com.x.program.center.Business;
import com.x.program.center.qiyeweixin.User.Extattr.Attr;

public class SyncOrganization {

    private static Logger logger = LoggerFactory.getLogger(SyncOrganization.class);

    protected static Gson gson = XGsonBuilder.instance();

    public PullResult execute(Business business) throws Exception {
        logger.print("开始与云之家同步人员组织,方向:拉入.");
        PullResult result = new PullResult();
        String accessToken = Config.yunzhijia().resAccessToken();
        logger.print("accessToken：" + accessToken);
        List<Unit> units = new ArrayList<>();
        List<Person> people = new ArrayList<>();
        List<PersonAttribute> personAttributes = new ArrayList<>();
        List<Identity> identities = new ArrayList<>();
        YunzhijiaFactory factory = new YunzhijiaFactory(accessToken);
        for (Department root : factory.roots()) {
            logger.print("开始同步云之家组织的信息:{}", XGsonBuilder.toJson(root));
            this.startCheck(business, result, units, people, personAttributes, identities, accessToken, factory, null, root);
        }
        // 处理兼职数据
        for (Company company : factory.getCompanys()) {
            // logger.print(XGsonBuilder.toJson(company));
            Person person = business.person().getWithYunzhijiaIdObject(company.getOpenId());
            Unit unit = business.unit().getWithYunzhijiaIdObject(Objects.toString(company.getOrgId()));
            // logger.print(XGsonBuilder.toJson(person));
            // logger.print(XGsonBuilder.toJson(unit));
            User u = new User();
            u.setWeights(company.getWeights());
            Identity identity = this.checkIdentity(business, result, person, unit, u, false);
            identities.add(identity);
        }
        logger.print("开始清理本地用户组织信息");
        this.clean(business, result, units, people, identities);
        CacheManager.notify(Person.class);
        CacheManager.notify(PersonAttribute.class);
        CacheManager.notify(Unit.class);
        CacheManager.notify(UnitAttribute.class);
        CacheManager.notify(UnitDuty.class);
        CacheManager.notify(Identity.class);
        result.end();
        if (!result.getCreateUnitList().isEmpty()) {
            logger.print("创建组织({}):{}.", result.getCreateUnitList().size(),
                    StringUtils.join(result.getCreateUnitList(), ","));
        }
        if (!result.getUpdateUnitList().isEmpty()) {
            logger.print("修改组织({}):{}.", result.getUpdateUnitList().size(),
                    StringUtils.join(result.getUpdateUnitList(), ","));
        }
        if (!result.getRemoveUnitList().isEmpty()) {
            logger.print("删除组织({}):{}.", result.getRemoveUnitList().size(),
                    StringUtils.join(result.getRemoveUnitList(), ","));
        }
        if (!result.getCreatePersonList().isEmpty()) {
            logger.print("创建个人({}):{}.", result.getCreatePersonList().size(),
                    StringUtils.join(result.getCreatePersonList(), ","));
        }
        if (!result.getUpdatePersonList().isEmpty()) {
            logger.print("修改个人({}):{}.", result.getUpdatePersonList().size(),
                    StringUtils.join(result.getUpdatePersonList(), ","));
        }
        if (!result.getRemovePersonList().isEmpty()) {
            logger.print("删除个人({}):{}.", result.getRemovePersonList().size(),
                    StringUtils.join(result.getRemovePersonList(), ","));
        }
        if (!result.getCreateIdentityList().isEmpty()) {
            logger.print("创建身份({}):{}.", result.getCreateIdentityList().size(),
                    StringUtils.join(result.getCreateIdentityList(), ","));
        }
        if (!result.getUpdateIdentityList().isEmpty()) {
            logger.print("修改身份({}):{}.", result.getUpdateIdentityList().size(),
                    StringUtils.join(result.getUpdateIdentityList(), ","));
        }
        if (!result.getRemoveIdentityList().isEmpty()) {
            logger.print("删除身份({}):{}.", result.getRemoveIdentityList().size(),
                    StringUtils.join(result.getRemoveIdentityList(), ","));
        }
        logger.print("结束云之家同步人员组织同步.");
        return result;
    }

    private void startCheck(Business business, PullResult result, List<Unit> units, List<Person> people,
                            List<PersonAttribute> personAttributes, List<Identity> identities, String accessToken,
                            YunzhijiaFactory factory, Unit sup, Department org) throws Exception {
        // 检查组织
        Unit unit = this.checkUnit(business, result, sup, org);
        units.add(unit);
        // 遍历组织下的所有人检查创建
        for (User o : factory.listUser(org)) {
            //
            Person person = this.checkPerson(business, result, accessToken, o);
            /* 如果人员没有手机号,那么就先跳过这个人 */
            if (null != person) {
                people.add(person);
                Identity identity = this.checkIdentity(business, result, person, unit, o, true);
                identities.add(identity);
            }
        }
        for (Department o : factory.listSub(org)) {
            this.startCheck(business, result, units, people, personAttributes, identities, accessToken, factory, unit, o);
        }
    }

    private Unit checkUnit(Business business, PullResult result, Unit sup, Department org) throws Exception {
        Unit unit = business.unit().getWithYunzhijiaIdObject(Objects.toString(org.getId()));
        // 如果找到了顶级组织
        if (null != unit) {
            if ((null == sup) && (StringUtils.isNotEmpty(unit.getSuperior()))) {
                /* 不是一个顶层组织所以只能删除重建 */
                removeUnit(business, result, unit);
                unit = null;
            }
            if ((null != sup) && (!StringUtils.equals(sup.getId(), unit.getSuperior()))) {
                /* 指定的上级部门和预期不符 */
                removeUnit(business, result, unit);
                unit = null;
            }
        }
        // 如果没有找到顶级组织
        if (null == unit) {
            unit = this.createUnit(business, result, sup, org);
        } else {
            if (!StringUtils.equals(unit.getYunzhijiaHash(), DigestUtils.sha256Hex(XGsonBuilder.toJson(org)))) {
                logger.print("组织【{}】的hash值变化，更新组织====", org.getName());
                unit = this.updateUnit(business, result, unit, org);
            }
        }
        return unit;
    }

    private Unit createUnit(Business business, PullResult result, Unit sup, Department org) throws Exception {
        EntityManagerContainer emc = business.entityManagerContainer();
        Unit unit = new Unit();
        emc.beginTransaction(Unit.class);
        unit.setName(org.getName());
        unit.setUnique(org.getUnique());
        unit.setYunzhijiaId(org.getId());
        List<String> typeList = new ArrayList<>();
        typeList.add(org.getBusinessUnit() == 1 ? "company" : "department");
        unit.setTypeList(typeList);
        unit.setYunzhijiaHash(DigestUtils.sha256Hex(XGsonBuilder.toJson(org)));
        if (null != sup) {
            unit.setSuperior(sup.getId());
        }
        if (null != org.getWeights()) {
            unit.setOrderNumber(org.getWeights().intValue());
        }
        business.unit().adjustInherit(unit);
        emc.persist(unit);
        emc.commit();
        result.getCreateUnitList().add(unit.getDistinguishedName());
        return unit;
    }

    private Unit updateUnit(Business business, PullResult result, Unit unit, Department department) throws Exception {
        EntityManagerContainer emc = business.entityManagerContainer();
        emc.beginTransaction(Unit.class);
        unit.setYunzhijiaHash(DigestUtils.sha256Hex(XGsonBuilder.toJson(department)));
        unit.setName(department.getName());
        unit.setUnique(department.getUnique());
        if (null != department.getWeights()) {
            unit.setOrderNumber(department.getWeights().intValue());
        }
        business.unit().adjustInherit(unit);
        emc.check(unit, CheckPersistType.all);
        emc.commit();
        this.updateIdentityUnitNameAndUnitLevelName(business, unit);
        result.getUpdateUnitList().add(unit.getDistinguishedName());
        return unit;
    }

    private Unit updateUnit(Business business, PullResult result, Unit unit) throws Exception {
        EntityManagerContainer emc = business.entityManagerContainer();
        emc.beginTransaction(Unit.class);
        List<String> typeList = new ArrayList<>();
        typeList.add("doBan");
        unit.setTypeList(typeList);
        emc.check(unit, CheckPersistType.all);
        emc.commit();
        result.getUpdateUnitList().add(unit.getDistinguishedName());
        return unit;
    }

    private void removeUnit(Business business, PullResult result, Unit unit) throws Exception {
        logger.print("正在删除组织{}.", unit.getDistinguishedName());
        List<Unit> os = new ArrayList<>();
        os.add(unit);
        os.addAll(business.unit().listSubNestedObject(unit));
        os = os.stream()
                .sorted(Comparator.comparing(Unit::getLevel, Comparator.nullsLast(Integer::compareTo)).reversed())
                .collect(Collectors.toList());
        for (Unit o : os) {
            this.removeSingleUnit(business, result, o);
        }
    }

    private void removeSingleUnit(Business business, PullResult result, Unit unit) throws Exception {
        logger.print("正在尝试删除单个组织{}.", unit.getDistinguishedName());
        EntityManagerContainer emc = business.entityManagerContainer();
        // 检查一下，该组织是否已经被删除过了
        unit = emc.find(unit.getId(), Unit.class);
        if (unit != null) {
            emc.beginTransaction(UnitAttribute.class);
            emc.beginTransaction(UnitDuty.class);
            emc.beginTransaction(Identity.class);
            emc.beginTransaction(Group.class);
            for (UnitAttribute o : emc.listEqual(UnitAttribute.class, UnitAttribute.unit_FIELDNAME, unit.getId())) {
                emc.remove(o, CheckRemoveType.all);
                result.getRemoveUnitAttributeList().add(o.getDistinguishedName());
            }
            for (UnitDuty o : emc.listEqual(UnitDuty.class, UnitDuty.unit_FIELDNAME, unit.getId())) {
                emc.remove(o, CheckRemoveType.all);
                result.getRemoveUnitDutyList().add(o.getDistinguishedName());
            }
            for (Identity o : emc.listEqual(Identity.class, Identity.unit_FIELDNAME, unit.getId())) {
                emc.remove(o, CheckRemoveType.all);
                result.getRemoveIdentityList().add(o.getDistinguishedName());

                for (Group group : business.group().listSupDirectWithIdentityObject(o.getId())) {
                    group.getIdentityList().remove(o.getId());
                    group.setIdentityList(group.getIdentityList());
                }
            }
            emc.commit();

            emc.beginTransaction(Unit.class);
            emc.remove(unit, CheckRemoveType.all);
            emc.commit();
            result.getRemoveUnitList().add(unit.getDistinguishedName());
        }

    }

    private Person checkPerson(Business business, PullResult result, String accessToken, User user) throws Exception {
        Person person = business.person().getWithYunzhijiaIdObject(user.getOpenId());
        if (user.getStatus().equals("1")) {
            user.setStatus("0");
        } else {
            user.setStatus("2");
        }
        if (null == person) {
            if ((StringUtils.isNotEmpty(user.getPhone())) && StringUtils.isNotEmpty(user.getName())) {
                person = this.createOrLinkPerson(business, result, user);
            }
        } else {
            if ((StringUtils.isNotEmpty(user.getPhone())) && StringUtils.isNotEmpty(user.getName())) {
                if (!StringUtils.equals(DigestUtils.sha256Hex(XGsonBuilder.toJson(user)), person.getYunzhijiaHash())) {
                    person = this.updatePerson(business, result, person, user);
                }
            }
        }
        return person;
    }

    private void doBanPerson(Person person, Business business) throws Exception{
        EntityManagerContainer emc = business.entityManagerContainer();
        emc.beginTransaction(Custom.class);
        emc.beginTransaction(Group.class);
        emc.beginTransaction(Role.class);
        emc.beginTransaction(UnitDuty.class);
        emc.beginTransaction(Identity.class);
        CustomPersonInfo personBanInfo = new CustomPersonInfo();
        personBanInfo.setOperator("xadmin");
        WrapPerson wrapPerson = WrapPerson.copier.copy(person);
        List<Group> groupList = business.group().listSupDirectWithPersonObject(person);
        for (Group group : groupList){
            group.getPersonList().remove(person.getId());
            group.setPersonList(group.getPersonList());
            wrapPerson.getGroupList().add(group.getId());
        }
        List<Role> roleList = business.role().listObjByPerson(person.getId());
        for (Role role : roleList){
            role.getPersonList().remove(person.getId());
            role.setPersonList(role.getPersonList());
            wrapPerson.getRoleList().add(role.getId());
        }
        List<Identity> identityList = emc.listEqual(Identity.class, Identity.person_FIELDNAME, person.getId());
        List<WrapIdentity> wrapIdentityList = new ArrayList<>();
        for (Identity identity : identityList){
            WrapIdentity wrapIdentity = WrapIdentity.copier.copy(identity);
            List<UnitDuty> unitDutyList = business.unit().listObjByIdentity(identity.getId());
            for (UnitDuty unitDuty : unitDutyList){
                unitDuty.getIdentityList().remove(identity.getId());
                unitDuty.setIdentityList(unitDuty.getIdentityList());
                wrapIdentity.getDutyList().add(unitDuty.getId());
            }
            groupList = business.group().listSupDirectWithIdentityObject(identity.getId());
            for (Group group : groupList){
                group.getIdentityList().remove(identity.getId());
                group.setIdentityList(group.getIdentityList());
                wrapIdentity.getGroupList().add(group.getId());
            }
            wrapIdentityList.add(wrapIdentity);
            emc.remove(identity);
        }
        personBanInfo.setPerson(wrapPerson);
        personBanInfo.setIdentityList(wrapIdentityList);
        String name = person.getId()+"#ban";
        Custom custom = getCustomWithName(emc, name);
        if(custom == null) {
            custom = new Custom();
            custom.setPerson(person.getDistinguishedName());
            custom.setName(name);
            custom.setData(gson.toJson(personBanInfo));
            emc.persist(custom);
        }else {
            custom.setData(gson.toJson(personBanInfo));
        }
    }

    private Custom getCustomWithName(EntityManagerContainer emc, String name) throws Exception {
        EntityManager em = emc.get(Custom.class);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Custom> cq = cb.createQuery(Custom.class);
        Root<Custom> root = cq.from(Custom.class);
        Predicate p = cb.equal(root.get(Custom_.name), name);
        List<Custom> list = em.createQuery(cq.where(p)).setMaxResults(1).getResultList();
        if (list.isEmpty()) {
            return null;
        } else {
            return list.get(0);
        }
    }

    private Person createOrLinkPerson(Business business, PullResult result, User user) throws Exception {
        EntityManagerContainer emc = business.entityManagerContainer();
        emc.beginTransaction(Person.class);
        Person person = emc.flag(user.getPhone(), Person.class);
        if (null != person) {
            person.setYunzhijiaId(Objects.toString(user.getOpenId()));
            person.setYunzhijiaHash(DigestUtils.sha256Hex(XGsonBuilder.toJson(user)));
            person.setName(user.getName());
            person.setMobile(user.getPhone());
            person.setUnique(user.getUnique());
            person.setMail(user.getEmail());
            person.setEmployee(user.getJobNo());
            // person.setOfficePhone(user.getTelephone());
            person.setGenderType(Objects.equals("1", user.getGender()) ? GenderType.m : GenderType.f);
            emc.check(person, CheckPersistType.all);
            result.getUpdatePersonList().add(person.getDistinguishedName());
        } else {
            person = new Person();
            person.setYunzhijiaId(Objects.toString(user.getOpenId()));
            person.setYunzhijiaHash(DigestUtils.sha256Hex(XGsonBuilder.toJson(user)));
            person.setName(user.getName());
            person.setMobile(user.getPhone());
            person.setUnique(user.getUnique());
            person.setEmployee(user.getJobNo());
            // person.setEmployee(user.getJobNumber());
            person.setMail(user.getEmail());
            // person.setOfficePhone(user.getTelephone());
            person.setGenderType(Objects.equals("1", user.getGender()) ? GenderType.m : GenderType.f);
            /* 新增人员需要增加密码 */
            business.person().setPassword(person, this.initPassword(business, person));
            emc.persist(person, CheckPersistType.all);
            result.getCreatePersonList().add(person.getDistinguishedName());
        }
        emc.commit();
        return person;
    }

    private String initPassword(Business business, Person person) throws Exception {
        String str = Config.person().getPassword();
        Pattern pattern = Pattern.compile(com.x.base.core.project.config.Person.REGULAREXPRESSION_SCRIPT);
        Matcher matcher = pattern.matcher(str);
        if (matcher.matches()) {
            Source source = GraalvmScriptingFactory.functionalization(StringEscapeUtils.unescapeJson(matcher.group(1)));
            GraalvmScriptingFactory.Bindings bindings = new GraalvmScriptingFactory.Bindings()
                    .putMember(GraalvmScriptingFactory.BINDING_NAME_SERVICE_PERSON, person);
            Optional<String> opt = GraalvmScriptingFactory.evalAsString(source, bindings);
            if (opt.isPresent()) {
                str = opt.get();
            }
        }
        return str;
    }

    private Person updatePerson(Business business, PullResult result, Person person, User user) throws Exception {
        EntityManagerContainer emc = business.entityManagerContainer();
        emc.beginTransaction(Person.class);
        person.setYunzhijiaHash(DigestUtils.sha256Hex(XGsonBuilder.toJson(user)));
        // person.setEmployee(user.getJobNumber());
        person.setName(user.getName());
        person.setMobile(user.getPhone());
        person.setMail(user.getEmail());
        person.setStatus(user.getStatus());
        person.setGenderType(Objects.equals("1", user.getGender()) ? GenderType.m : GenderType.f);
        // person.setUnique(user.getUnique());
        // person.setOfficePhone(user.getTelephone());
        emc.check(person, CheckPersistType.all);
        emc.commit();
        result.getUpdatePersonList().add(person.getDistinguishedName());
        return person;
    }

    private void removePerson(Business business, PullResult result, Person person) throws Exception {
        EntityManagerContainer emc = business.entityManagerContainer();
        emc.beginTransaction(PersonAttribute.class);
        for (PersonAttribute o : emc.listEqual(PersonAttribute.class, PersonAttribute.person_FIELDNAME,
                person.getId())) {
            result.getRemovePersonAttributeList().add(o.getDistinguishedName());
            emc.remove(o, CheckRemoveType.all);
        }
        emc.commit();

        emc.beginTransaction(Group.class);
        for(Group group : business.group().listSupDirectWithPersonObject(person.getId())){
            group.getPersonList().remove(person.getId());
            group.setPersonList(group.getPersonList());
        }
        emc.commit();

        emc.beginTransaction(Person.class);
        emc.remove(person, CheckRemoveType.all);
        emc.commit();
        result.getRemovePersonList().add(person.getDistinguishedName());
    }

    private Identity checkIdentity(Business business, PullResult result, Person person, Unit unit, User user, Boolean isMajor)
            throws Exception {
        EntityManagerContainer emc = business.entityManagerContainer();
        EntityManager em = emc.get(Identity.class);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Identity> cq = cb.createQuery(Identity.class);
        Root<Identity> root = cq.from(Identity.class);
        Predicate p = cb.equal(root.get(Identity_.person), person.getId());
        p = cb.and(p, cb.equal(root.get(Identity_.unit), unit.getId()));
        List<Identity> os = em.createQuery(cq.select(root).where(p)).setMaxResults(1).getResultList();
        Identity identity = null;
        Long order = null;
        if (StringUtils.isNotEmpty(user.getWeights())) {
            order = Long.parseLong(user.getWeights());
        }
        if (os.size() == 0) {
            if (person.getStatus().equals("0")) {
                identity = this.createIdentity(business, result, person, unit, user, order, isMajor);
            }
        } else {
            identity = os.get(0);
            identity = this.updateIdentity(business, result, unit, identity, user, order);
        }
        return identity;
    }

    private Identity createIdentity(Business business, PullResult result, Person person, Unit unit, User user,
                                    Long order, Boolean isMajor) throws Exception {
        EntityManagerContainer emc = business.entityManagerContainer();
        emc.beginTransaction(Identity.class);
        Identity identity = new Identity();
        identity.setUnique(unit.getUnique() + "_" + person.getUnique());
        identity.setName(person.getName());
        identity.setPerson(person.getId());
        identity.setUnit(unit.getId());
        identity.setUnitLevel(unit.getLevel());
        identity.setUnitLevelName(unit.getLevelName());
        identity.setUnitName(unit.getName());
        if (order != null) {
            identity.setOrderNumber(order.intValue());
        }
        identity.setMajor(isMajor);
        emc.persist(identity, CheckPersistType.all);
        emc.commit();
        result.getCreateIdentityList().add(identity.getDistinguishedName());
        return identity;
    }

    private Identity updateIdentity(Business business, PullResult result, Unit unit, Identity identity, User user,
                                    Long order) throws Exception {
        if (null != order) {
            if (!StringUtils.equals(Objects.toString(identity.getOrderNumber(), ""), Objects.toString(order, ""))) {
                EntityManagerContainer emc = business.entityManagerContainer();
                emc.beginTransaction(Identity.class);
                if (order != null) {
                    identity.setOrderNumber(order.intValue());
                }
                // 不更新唯一编码
                // identity.setUnique(unit.getUnique());
                emc.commit();
                result.getUpdateIdentityList().add(identity.getDistinguishedName());
            }
        }
        return identity;
    }

    private void updateIdentityUnitNameAndUnitLevelName(Business business, Unit unit) throws Exception {
        EntityManagerContainer emc = business.entityManagerContainer();
        List<Unit> os = new ArrayList<>();
        os.add(unit);
        os.addAll(business.unit().listSubNestedObject(unit));

        for (Unit u : os) {
            List<Identity> identityList = this.pickIdentitiesByUnit(business, u.getId());
            if (ListTools.isNotEmpty(identityList)) {
                String _unitName = u.getName();
                String _unitLevelName = u.getLevelName();

                emc.beginTransaction(Identity.class);
                for (Identity i : identityList) {
                    i.setUnitName(_unitName);
                    i.setUnitLevelName(_unitLevelName);
                    emc.check(i, CheckPersistType.all);
                }
                emc.commit();
            }

        }
    }

    private List<Identity> pickIdentitiesByUnit(Business business, String unit) throws Exception {
        EntityManager em = business.entityManagerContainer().get(Identity.class);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Identity> cq = cb.createQuery(Identity.class);
        Root<Identity> root = cq.from(Identity.class);
        Predicate p = cb.equal(root.get(Identity_.unit), unit);
        return em.createQuery(cq.select(root).where(p)).getResultList();
    }

    private void clean(Business business, PullResult result, List<Unit> units, List<Person> people,
                       List<Identity> identities) throws Exception {
        EntityManagerContainer emc = business.entityManagerContainer();
        /* 组织单独方法删除 */
        List<Unit> allUnit = this.listUnit(business);
        List<Unit> removeUnits = ListUtils.subtract(allUnit, units).stream()
                .sorted(Comparator.comparing(Unit::getLevel, Comparator.nullsLast(Integer::compareTo)))
                .collect(Collectors.toList());
        for (Unit unit : removeUnits) {
            // this.removeSingleUnit(business, result, unit);
            // 修改组织类别
            if (!unit.getTypeList().contains("doBan")) {
                this.updateUnit(business, result, unit);
            }
        }
        List<Person> allPeople = this.listPerson(business);
        /* 删除个人 */
//        for (Person person : ListUtils.subtract(allPeople, people)) {
//            logger.print("删除用户：{}", person.getDistinguishedName());
//            this.removePerson(business, result, person);
//        }

        /* 禁用个人 */
        for (Person person : ListUtils.subtract(allPeople, people)) {
            if (person.getStatus().equals("2")) {
                continue;
            }
            logger.print("禁用用户：{}", person.getDistinguishedName());
            emc.beginTransaction(Person.class);
            Person entityPerson = emc.find(person.getId(), Person.class);
            entityPerson.setStatus(PersonStatusEnum.BAN.getValue());
            entityPerson.setStatusDes("云之家禁用");
            emc.check(entityPerson, CheckPersistType.all);
            this.doBanPerson(person, business);
            emc.commit();
            CacheManager.notify(UnitDuty.class);
            CacheManager.notify(Identity.class);
            CacheManager.notify(Person.class);
            CacheManager.notify(Role.class);
            CacheManager.notify(Group.class);
        }

        List<Identity> allIdentities = this.listIdentity(business);
        /* 删除身份 */
        for (Identity identity : ListUtils.subtract(allIdentities, identities)) {
            Person person = emc.find(identity.getPerson(), Person.class);
            if (null == person || StringUtils.isNotEmpty(person.getYunzhijiaId())) {
                List<UnitDuty> uds = emc.listIsMember(UnitDuty.class, UnitDuty.identityList_FIELDNAME,
                        identity.getId());
                if (ListTools.isNotEmpty(uds)) {
                    emc.beginTransaction(UnitDuty.class);
                    uds.stream().forEach(o -> {
                        o.getIdentityList().remove(identity.getId());
                    });
                    emc.commit();
                }
                emc.beginTransaction(Identity.class);
                emc.remove(identity, CheckRemoveType.all);
                emc.commit();
            }
        }
    }

    private List<Unit> listUnit(Business business) throws Exception {
        EntityManagerContainer emc = business.entityManagerContainer();
        EntityManager em = emc.get(Unit.class);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Unit> cq = cb.createQuery(Unit.class);
        Root<Unit> root = cq.from(Unit.class);
        Predicate p = cb.notEqual(root.get(Unit_.yunzhijiaId), "");
        p = cb.and(p, cb.isNotNull(root.get(Unit_.yunzhijiaId)));
        List<Unit> os = em.createQuery(cq.select(root).where(p)).getResultList();
        return os;
    }

    private List<Person> listPerson(Business business) throws Exception {
        EntityManagerContainer emc = business.entityManagerContainer();
        EntityManager em = emc.get(Person.class);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Person> cq = cb.createQuery(Person.class);
        Root<Person> root = cq.from(Person.class);
        Predicate p = cb.notEqual(root.get(Person_.yunzhijiaId), "");
        p = cb.and(p, cb.isNotNull(root.get(Person_.yunzhijiaId)));
        List<Person> os = em.createQuery(cq.select(root).where(p)).getResultList();
        return os;
    }

    private List<Identity> listIdentity(Business business) throws Exception {
        return business.entityManagerContainer().listAll(Identity.class);
    }

    public static class PullResult extends GsonPropertyObject {

        private Date start = new Date();

        private Date end;

        private Long elapsed;

        private List<String> createUnitList = new ArrayList<>();
        private List<String> updateUnitList = new ArrayList<>();
        private List<String> removeUnitList = new ArrayList<>();

        private List<String> createPersonList = new ArrayList<>();
        private List<String> updatePersonList = new ArrayList<>();
        private List<String> removePersonList = new ArrayList<>();

        private List<String> createIdentityList = new ArrayList<>();
        private List<String> updateIdentityList = new ArrayList<>();
        private List<String> removeIdentityList = new ArrayList<>();

        private List<String> createPersonAttributeList = new ArrayList<>();
        private List<String> updatePersonAttributeList = new ArrayList<>();
        private List<String> removePersonAttributeList = new ArrayList<>();

        private List<String> removeUnitDutyList = new ArrayList<>();
        private List<String> removeUnitAttributeList = new ArrayList<>();

        public void end() {
            this.end = new Date();
            this.elapsed = end.getTime() - start.getTime();
        }

        public Date getStart() {
            return start;
        }

        public void setStart(Date start) {
            this.start = start;
        }

        public Date getEnd() {
            return end;
        }

        public void setEnd(Date end) {
            this.end = end;
        }

        public List<String> getCreateUnitList() {
            return createUnitList;
        }

        public void setCreateUnitList(List<String> createUnitList) {
            this.createUnitList = createUnitList;
        }

        public List<String> getUpdateUnitList() {
            return updateUnitList;
        }

        public void setUpdateUnitList(List<String> updateUnitList) {
            this.updateUnitList = updateUnitList;
        }

        public List<String> getCreatePersonList() {
            return createPersonList;
        }

        public void setCreatePersonList(List<String> createPersonList) {
            this.createPersonList = createPersonList;
        }

        public List<String> getUpdatePersonList() {
            return updatePersonList;
        }

        public void setUpdatePersonList(List<String> updatePersonList) {
            this.updatePersonList = updatePersonList;
        }

        public List<String> getCreateIdentityList() {
            return createIdentityList;
        }

        public void setCreateIdentityList(List<String> createIdentityList) {
            this.createIdentityList = createIdentityList;
        }

        public List<String> getRemoveUnitList() {
            return removeUnitList;
        }

        public void setRemoveUnitList(List<String> removeUnitList) {
            this.removeUnitList = removeUnitList;
        }

        public List<String> getRemovePersonList() {
            return removePersonList;
        }

        public void setRemovePersonList(List<String> removePersonList) {
            this.removePersonList = removePersonList;
        }

        public List<String> getRemoveIdentityList() {
            return removeIdentityList;
        }

        public void setRemoveIdentityList(List<String> removeIdentityList) {
            this.removeIdentityList = removeIdentityList;
        }

        public List<String> getRemoveUnitDutyList() {
            return removeUnitDutyList;
        }

        public void setRemoveUnitDutyList(List<String> removeUnitDutyList) {
            this.removeUnitDutyList = removeUnitDutyList;
        }

        public List<String> getRemoveUnitAttributeList() {
            return removeUnitAttributeList;
        }

        public void setRemoveUnitAttributeList(List<String> removeUnitAttributeList) {
            this.removeUnitAttributeList = removeUnitAttributeList;
        }

        public List<String> getRemovePersonAttributeList() {
            return removePersonAttributeList;
        }

        public void setRemovePersonAttributeList(List<String> removePersonAttributeList) {
            this.removePersonAttributeList = removePersonAttributeList;
        }

        public Long getElapsed() {
            return elapsed;
        }

        public void setElapsed(Long elapsed) {
            this.elapsed = elapsed;
        }

        public List<String> getUpdateIdentityList() {
            return updateIdentityList;
        }

        public void setUpdateIdentityList(List<String> updateIdentityList) {
            this.updateIdentityList = updateIdentityList;
        }

        public List<String> getCreatePersonAttributeList() {
            return createPersonAttributeList;
        }

        public void setCreatePersonAttributeList(List<String> createPersonAttributeList) {
            this.createPersonAttributeList = createPersonAttributeList;
        }

        public List<String> getUpdatePersonAttributeList() {
            return updatePersonAttributeList;
        }

        public void setUpdatePersonAttributeList(List<String> updatePersonAttributeList) {
            this.updatePersonAttributeList = updatePersonAttributeList;
        }
    }

    protected static class CustomPersonInfo extends GsonPropertyObject{
        @FieldDescribe("操作人")
        private String operator;
        @FieldDescribe("操作时间")
        private Date operateTime;
        @FieldDescribe("被操作用户对象")
        private WrapPerson person;
        private List<WrapIdentity> identityList;

        public String getOperator() {
            return operator;
        }

        public void setOperator(String operator) {
            this.operator = operator;
        }

        public Date getOperateTime() {
            return operateTime;
        }

        public void setOperateTime(Date operateTime) {
            this.operateTime = operateTime;
        }

        public WrapPerson getPerson() {
            return person;
        }

        public void setPerson(WrapPerson person) {
            this.person = person;
        }

        public List<WrapIdentity> getIdentityList() {
            return identityList;
        }

        public void setIdentityList(List<WrapIdentity> identityList) {
            this.identityList = identityList;
        }
    }

    public static class WrapPerson extends Person{
        static WrapCopier<Person, WrapPerson> copier = WrapCopierFactory.wo(Person.class, WrapPerson.class, null,
                ListTools.toList(JpaObject.FieldsInvisible));
        private List<String> groupList = new ArrayList<>();
        private List<String> roleList = new ArrayList<>();

        public List<String> getGroupList() {
            return groupList == null ? new ArrayList<>() : groupList;
        }

        public void setGroupList(List<String> groupList) {
            this.groupList = groupList;
        }

        public List<String> getRoleList() {
            return roleList == null ? new ArrayList<>() : roleList;
        }

        public void setRoleList(List<String> roleList) {
            this.roleList = roleList;
        }
    }

    public static class WrapIdentity extends Identity {
        static WrapCopier<Identity, WrapIdentity> copier = WrapCopierFactory.wo(Identity.class, WrapIdentity.class, null,
                ListTools.toList(JpaObject.FieldsInvisible));
        static WrapCopier<WrapIdentity, Identity> copierIn = WrapCopierFactory.wo(WrapIdentity.class, Identity.class, null,
                ListTools.toList(JpaObject.FieldsInvisible));
        private List<String> groupList = new ArrayList<>();
        private List<String> dutyList = new ArrayList<>();

        public List<String> getGroupList() {
            return groupList == null ? new ArrayList<>() : groupList;
        }

        public void setGroupList(List<String> groupList) {
            this.groupList = groupList;
        }

        public List<String> getDutyList() {
            return dutyList == null ? new ArrayList<>() : dutyList;
        }

        public void setDutyList(List<String> dutyList) {
            this.dutyList = dutyList;
        }
    }
}
