package xyz.weechang.moreco.component.gen.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.IoUtil;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.weechang.moreco.component.gen.model.dto.ClassInfo;
import xyz.weechang.moreco.component.gen.model.dto.CodeGenRequest;
import xyz.weechang.moreco.component.gen.model.dto.EntityInfo;
import xyz.weechang.moreco.component.gen.service.CodeGenService;
import xyz.weechang.moreco.core.exception.AppException;

import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author zhangwei
 * date 2019/2/27
 * time 14:21
 */
@Service
public class CodeGenServiceImpl implements CodeGenService {

    @Autowired
    private EntityManagerFactory sessionFactory;

    @Override
    public List<EntityInfo> getEntities() {
        List<EntityInfo> entityInfos = new ArrayList<>();
        Metamodel metamodel = sessionFactory.getMetamodel();
        Set<EntityType<?>> entityTypes = metamodel.getEntities();
        for (EntityType entityType : entityTypes) {
            EntityInfo entityInfo = new EntityInfo();
            entityInfo.setClazz(entityType.getBindableJavaType().getName());
            entityInfo.setEntityName(entityType.getName());
            entityInfos.add(entityInfo);
        }
        return entityInfos;
    }

    @Override
    public byte[] generatorCode(CodeGenRequest codeGenRequest) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(outputStream);
        generator(codeGenRequest, zip);
        IoUtil.close(zip);
        return outputStream.toByteArray();
    }

    private void generator(CodeGenRequest request, ZipOutputStream zip) {
        List<String> classes = request.getClasses();
        for (String clazz : classes) {
            ClassInfo classInfo = new ClassInfo(clazz);
            //设置velocity资源加载器
            Properties prop = new Properties();
            prop.put("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
            Velocity.init(prop);

            //获取模板列表
            List<String> templates = getTemplates();
            for (String template : templates) {
                //渲染模板
                StringWriter sw = new StringWriter();
                Template tpl = Velocity.getTemplate(template, "UTF-8");
                //封装模板数据
                Map<String, Object> map = new HashMap<>();
                map.put("classInfo", classInfo);
                map.put("request", request);
                map.put("date", DateUtil.formatDate(new Date()));
                map.put("time", DateUtil.formatTime(new Date()));
                VelocityContext context = new VelocityContext(map);
                tpl.merge(context, sw);
                try {
                    zip.putNextEntry(new ZipEntry(getFileName(template, request.getPackageName(), classInfo.getClazzSimpleName())));
                    IoUtil.write(zip, "UTF-8", false, sw.toString());
                    IoUtil.close(sw);
                } catch (IOException e) {
                    throw new AppException("渲染模板失败，表名：", e);
                }
            }
            IoUtil.close(zip);
        }

    }

    private List<String> getTemplates() {
        List<String> templates = new ArrayList<String>();
        templates.add("template/Controller.java.vm");
        templates.add("template/Dao.java.vm");
        templates.add("template/QueryDto.java.vm");
        templates.add("template/SaveDto.java.vm");
        templates.add("template/Service.java.vm");
        templates.add("template/ServiceImpl.java.vm");
        return templates;
    }

    private String getFileName(String template, String packageName, String clazzSimpleName) {
        String fileName = null;
        String templateName = template.replace("template/", "").replace(".vm", "");
        String file = clazzSimpleName + templateName;
        String filePath = "";
        fileName = filePath + file;
        return fileName;
    }
}