package cn.jestar.convert.weapon;

import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import cn.jestar.convert.BaseConvertor;
import cn.jestar.convert.Constants;
import cn.jestar.convert.bean.TranslatedBean;
import cn.jestar.convert.utils.JsonUtils;
import cn.jestar.convert.utils.RegexUtils;

/**
 * 用与武器翻译的类
 * Created by 花京院 on 2019/2/4.
 */

public class WeaponConvertor extends BaseConvertor {
    public static final String REGEX = "<a href=\"../(ida/\\d+\\.html)\">.*</a> x\\d+<br>";
    public static final String REGEX1 = "<span style=\"background-color:#.*;\">入手端材：<a href=\"../(ida/\\d+\\.html)\">.*</a> x\\d+</span><br>";
    private final File mSummaryFile;
    private final File mTranslatedFile;
    private final File mTranslationFile;
    private String mWeapon = "weapon";


    public WeaponConvertor(String name) {
        setName(name);
        mSummaryFile = new File(Constants.TEMP_SUMMARY_PATH, mWeapon);
        mTranslatedFile = new File(Constants.TEMP_TRANSLATED_PATH, mWeapon);
        mTranslationFile = new File(Constants.TEMP_TRANSLATION_PATH, mWeapon);
        mTransBeanFile=new File(mTranslatedFile,mTransBeanName);
    }

    /**
     * 根据名字创建对应的TranslatedBean{@link TranslatedBean}
     *
     * @param list 该类武器相关的url列表
     * @throws Exception
     */
    public void makeBean(List<String> list) throws Exception {
        String name = mJsonFileName;
        Map<String, String> map = getMap(name, mSummaryFile);
        TranslatedBean bean = new TranslatedBean();
        TreeSet<String> strings = new TreeSet<>(list);
        strings.addAll(map.values());
        bean.setUrls(new ArrayList<>(strings));
        map = getMap(name, mTranslatedFile);
        bean.setTexts(map);
        FileWriter writer = new FileWriter(mTransBeanFile);
        writer.write(JsonUtils.toString(bean));
        writer.close();
    }


    private Map<String, String> getMap(String name, File file) throws FileNotFoundException {
        FileReader reader = new FileReader(new File(file, name));
        Type type = new TypeToken<TreeMap<String, String>>() {
        }.getType();
        return JsonUtils.fromStringByType(reader, type);
    }

    /**
     * 比较Translation和Translated中同名的文件，获取Translated中非翻译的名字集合
     *
     * @return Translated中没有的名字集合
     * @throws Exception
     */
    public List<String> getNotTranslatedNames() throws Exception {
        File source = new File(mTranslationFile, mJsonFileName);
        List<String> list = JsonUtils.toList(new FileReader(source), String.class);
        TreeMap<String, String> treeMap = JsonUtils.fromString(new FileReader(new File(mTranslatedFile, mJsonFileName)), TreeMap.class);
        ArrayList<String> list1 = new ArrayList<>();
        for (String s : list) {
            String s1 = treeMap.get(s);
            if (s1 == null || s1.isEmpty()) {
                list1.add(s);
            }
        }
        return list1;
    }

    /**
     * 比较Translation和Translated中同名的文件，获取Translation中漏掉的名字
     *
     * @return translation中漏掉的名字集合
     * @throws Exception
     */
    public List<String> getLostNamesInTranslation() throws Exception {
        String name = mJsonFileName;
        File source = new File(mTranslationFile, name);
        List<String> list = JsonUtils.toList(new FileReader(source), String.class);
        TreeMap<String, String> treeMap = JsonUtils.fromString(new FileReader(new File(mTranslatedFile, name)), TreeMap.class);
        ArrayList<String> list1 = new ArrayList<>();
        for (String s : treeMap.keySet()) {
            if (list.indexOf(s) == -1) {
                list1.add(s);
            }
        }
        return list1;
    }


    /**
     * 解析相关素材的Url并保存
     *
     * @param text  一行文本
     * @param links 保存url的Set
     */
    public void getUrls(String text, Set<String> links) {
        if (text.matches(REGEX)) {
            links.add(RegexUtils.getMatchText(text, REGEX));
        } else if (text.matches(REGEX1)) {
            links.add(RegexUtils.getMatchText(text, REGEX1));
        }
    }

    /**
     * 读取文件，保存在StringBuilder中，并解析相关素材的url并保存
     *
     * @param url   文件的url
     * @param links 用与收集素材的url的Set
     * @return 文件的String
     * @throws Exception
     */
    public StringBuilder getText(String url, Set<String> links) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(Constants.MH_PATH + url));
        StringBuilder builder = new StringBuilder();
        String text;
        String separator = System.lineSeparator();
        boolean isIda = url.contains("ida");
        while ((text = reader.readLine()) != null) {
            text = text.trim();
            if (!text.isEmpty()) {
                builder.append(text).append(separator);
                if (isIda && links != null) {
                    getUrls(text, links);
                }
            }
        }
        reader.close();
        return builder;
    }
}