
package com.elcuk.jaxb;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for LanguageStringType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="LanguageStringType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Abkhazian"/>
 *     &lt;enumeration value="Adygei"/>
 *     &lt;enumeration value="Afar"/>
 *     &lt;enumeration value="Afrikaans"/>
 *     &lt;enumeration value="Albanian"/>
 *     &lt;enumeration value="Alsatian"/>
 *     &lt;enumeration value="Amharic"/>
 *     &lt;enumeration value="Arabic"/>
 *     &lt;enumeration value="Aramaic"/>
 *     &lt;enumeration value="Armenian"/>
 *     &lt;enumeration value="Assamese"/>
 *     &lt;enumeration value="Aymara"/>
 *     &lt;enumeration value="Azerbaijani"/>
 *     &lt;enumeration value="Bambara"/>
 *     &lt;enumeration value="Bashkir"/>
 *     &lt;enumeration value="Basque"/>
 *     &lt;enumeration value="Bengali"/>
 *     &lt;enumeration value="Berber"/>
 *     &lt;enumeration value="Bhutani"/>
 *     &lt;enumeration value="Bihari"/>
 *     &lt;enumeration value="Bislama"/>
 *     &lt;enumeration value="Breton"/>
 *     &lt;enumeration value="Bulgarian"/>
 *     &lt;enumeration value="Burmese"/>
 *     &lt;enumeration value="Buryat"/>
 *     &lt;enumeration value="Byelorussian"/>
 *     &lt;enumeration value="CantoneseChinese"/>
 *     &lt;enumeration value="Castillian"/>
 *     &lt;enumeration value="Catalan"/>
 *     &lt;enumeration value="Cayuga"/>
 *     &lt;enumeration value="Cheyenne"/>
 *     &lt;enumeration value="Chinese"/>
 *     &lt;enumeration value="ClassicalNewari"/>
 *     &lt;enumeration value="Cornish"/>
 *     &lt;enumeration value="Corsican"/>
 *     &lt;enumeration value="Creole"/>
 *     &lt;enumeration value="CrimeanTatar"/>
 *     &lt;enumeration value="Croatian"/>
 *     &lt;enumeration value="Czech"/>
 *     &lt;enumeration value="Danish"/>
 *     &lt;enumeration value="Dargwa"/>
 *     &lt;enumeration value="Dutch"/>
 *     &lt;enumeration value="English"/>
 *     &lt;enumeration value="Esperanto"/>
 *     &lt;enumeration value="Estonian"/>
 *     &lt;enumeration value="Faroese"/>
 *     &lt;enumeration value="Farsi"/>
 *     &lt;enumeration value="Fiji"/>
 *     &lt;enumeration value="Filipino"/>
 *     &lt;enumeration value="Finnish"/>
 *     &lt;enumeration value="Flemish"/>
 *     &lt;enumeration value="French"/>
 *     &lt;enumeration value="FrenchCanadian"/>
 *     &lt;enumeration value="Frisian"/>
 *     &lt;enumeration value="Galician"/>
 *     &lt;enumeration value="Georgian"/>
 *     &lt;enumeration value="German"/>
 *     &lt;enumeration value="Gibberish"/>
 *     &lt;enumeration value="Greek"/>
 *     &lt;enumeration value="Greenlandic"/>
 *     &lt;enumeration value="Guarani"/>
 *     &lt;enumeration value="Gujarati"/>
 *     &lt;enumeration value="Gullah"/>
 *     &lt;enumeration value="Hausa"/>
 *     &lt;enumeration value="Hawaiian"/>
 *     &lt;enumeration value="Hebrew"/>
 *     &lt;enumeration value="Hindi"/>
 *     &lt;enumeration value="Hmong"/>
 *     &lt;enumeration value="Hungarian"/>
 *     &lt;enumeration value="Icelandic"/>
 *     &lt;enumeration value="IndoEuropean"/>
 *     &lt;enumeration value="Indonesian"/>
 *     &lt;enumeration value="Ingush"/>
 *     &lt;enumeration value="Interlingua"/>
 *     &lt;enumeration value="Interlingue"/>
 *     &lt;enumeration value="Inuktitun"/>
 *     &lt;enumeration value="Inuktitut"/>
 *     &lt;enumeration value="Inupiak"/>
 *     &lt;enumeration value="Inupiaq"/>
 *     &lt;enumeration value="Irish"/>
 *     &lt;enumeration value="Italian"/>
 *     &lt;enumeration value="Japanese"/>
 *     &lt;enumeration value="Javanese"/>
 *     &lt;enumeration value="Kalaallisut"/>
 *     &lt;enumeration value="Kalmyk"/>
 *     &lt;enumeration value="Kannada"/>
 *     &lt;enumeration value="KarachayBalkar"/>
 *     &lt;enumeration value="Kashmiri"/>
 *     &lt;enumeration value="Kashubian"/>
 *     &lt;enumeration value="Kazakh"/>
 *     &lt;enumeration value="Khmer"/>
 *     &lt;enumeration value="Kinyarwanda"/>
 *     &lt;enumeration value="Kirghiz"/>
 *     &lt;enumeration value="Kirundi"/>
 *     &lt;enumeration value="Klingon"/>
 *     &lt;enumeration value="Korean"/>
 *     &lt;enumeration value="Kurdish"/>
 *     &lt;enumeration value="Ladino"/>
 *     &lt;enumeration value="Lao"/>
 *     &lt;enumeration value="Lapp"/>
 *     &lt;enumeration value="Latin"/>
 *     &lt;enumeration value="Latvian"/>
 *     &lt;enumeration value="Lingala"/>
 *     &lt;enumeration value="Lithuanian"/>
 *     &lt;enumeration value="Lojban"/>
 *     &lt;enumeration value="LowerSorbian"/>
 *     &lt;enumeration value="Macedonian"/>
 *     &lt;enumeration value="Malagasy"/>
 *     &lt;enumeration value="Malay"/>
 *     &lt;enumeration value="Malayalam"/>
 *     &lt;enumeration value="Maltese"/>
 *     &lt;enumeration value="MandarinChinese"/>
 *     &lt;enumeration value="Maori"/>
 *     &lt;enumeration value="Marathi"/>
 *     &lt;enumeration value="Mende"/>
 *     &lt;enumeration value="MiddleEnglish"/>
 *     &lt;enumeration value="Mirandese"/>
 *     &lt;enumeration value="Moksha"/>
 *     &lt;enumeration value="Moldavian"/>
 *     &lt;enumeration value="Mongo"/>
 *     &lt;enumeration value="Mongolian"/>
 *     &lt;enumeration value="Multilingual"/>
 *     &lt;enumeration value="Nauru"/>
 *     &lt;enumeration value="Navaho"/>
 *     &lt;enumeration value="Nepali"/>
 *     &lt;enumeration value="Nogai"/>
 *     &lt;enumeration value="Norwegian"/>
 *     &lt;enumeration value="Occitan"/>
 *     &lt;enumeration value="OldEnglish"/>
 *     &lt;enumeration value="Oriya"/>
 *     &lt;enumeration value="Oromo"/>
 *     &lt;enumeration value="Pashto"/>
 *     &lt;enumeration value="Persian"/>
 *     &lt;enumeration value="PigLatin"/>
 *     &lt;enumeration value="Polish"/>
 *     &lt;enumeration value="Portuguese"/>
 *     &lt;enumeration value="Punjabi"/>
 *     &lt;enumeration value="Quechua"/>
 *     &lt;enumeration value="Romance"/>
 *     &lt;enumeration value="Romanian"/>
 *     &lt;enumeration value="Romany"/>
 *     &lt;enumeration value="Russian"/>
 *     &lt;enumeration value="Samaritan"/>
 *     &lt;enumeration value="Samoan"/>
 *     &lt;enumeration value="Sangho"/>
 *     &lt;enumeration value="Sanskrit"/>
 *     &lt;enumeration value="Serbian"/>
 *     &lt;enumeration value="Serbo-Croatian"/>
 *     &lt;enumeration value="Sesotho"/>
 *     &lt;enumeration value="Setswana"/>
 *     &lt;enumeration value="Shona"/>
 *     &lt;enumeration value="SichuanYi"/>
 *     &lt;enumeration value="Sicilian"/>
 *     &lt;enumeration value="SignLanguage"/>
 *     &lt;enumeration value="Sindhi"/>
 *     &lt;enumeration value="Sinhalese"/>
 *     &lt;enumeration value="Siswati"/>
 *     &lt;enumeration value="Slavic"/>
 *     &lt;enumeration value="Slovak"/>
 *     &lt;enumeration value="Slovakian"/>
 *     &lt;enumeration value="Slovene"/>
 *     &lt;enumeration value="Somali"/>
 *     &lt;enumeration value="Spanish"/>
 *     &lt;enumeration value="Sumerian"/>
 *     &lt;enumeration value="Sundanese"/>
 *     &lt;enumeration value="Swahili"/>
 *     &lt;enumeration value="Swedish"/>
 *     &lt;enumeration value="SwissGerman"/>
 *     &lt;enumeration value="Syriac"/>
 *     &lt;enumeration value="Tagalog"/>
 *     &lt;enumeration value="TaiwaneseChinese"/>
 *     &lt;enumeration value="Tajik"/>
 *     &lt;enumeration value="Tamil"/>
 *     &lt;enumeration value="Tatar"/>
 *     &lt;enumeration value="Telugu"/>
 *     &lt;enumeration value="Thai"/>
 *     &lt;enumeration value="Tibetan"/>
 *     &lt;enumeration value="Tigrinya"/>
 *     &lt;enumeration value="Tonga"/>
 *     &lt;enumeration value="Tsonga"/>
 *     &lt;enumeration value="Turkish"/>
 *     &lt;enumeration value="Turkmen"/>
 *     &lt;enumeration value="Twi"/>
 *     &lt;enumeration value="Udmurt"/>
 *     &lt;enumeration value="Uighur"/>
 *     &lt;enumeration value="Ukrainian"/>
 *     &lt;enumeration value="Ukranian"/>
 *     &lt;enumeration value="Unknown"/>
 *     &lt;enumeration value="Urdu"/>
 *     &lt;enumeration value="Uzbek"/>
 *     &lt;enumeration value="Vietnamese"/>
 *     &lt;enumeration value="Volapuk"/>
 *     &lt;enumeration value="Welsh"/>
 *     &lt;enumeration value="Wolof"/>
 *     &lt;enumeration value="Xhosa"/>
 *     &lt;enumeration value="Yiddish"/>
 *     &lt;enumeration value="Yoruba"/>
 *     &lt;enumeration value="Zhuang"/>
 *     &lt;enumeration value="Zulu"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "LanguageStringType")
@XmlEnum
public enum LanguageStringType {

    @XmlEnumValue("Abkhazian")
    ABKHAZIAN("Abkhazian"),
    @XmlEnumValue("Adygei")
    ADYGEI("Adygei"),
    @XmlEnumValue("Afar")
    AFAR("Afar"),
    @XmlEnumValue("Afrikaans")
    AFRIKAANS("Afrikaans"),
    @XmlEnumValue("Albanian")
    ALBANIAN("Albanian"),
    @XmlEnumValue("Alsatian")
    ALSATIAN("Alsatian"),
    @XmlEnumValue("Amharic")
    AMHARIC("Amharic"),
    @XmlEnumValue("Arabic")
    ARABIC("Arabic"),
    @XmlEnumValue("Aramaic")
    ARAMAIC("Aramaic"),
    @XmlEnumValue("Armenian")
    ARMENIAN("Armenian"),
    @XmlEnumValue("Assamese")
    ASSAMESE("Assamese"),
    @XmlEnumValue("Aymara")
    AYMARA("Aymara"),
    @XmlEnumValue("Azerbaijani")
    AZERBAIJANI("Azerbaijani"),
    @XmlEnumValue("Bambara")
    BAMBARA("Bambara"),
    @XmlEnumValue("Bashkir")
    BASHKIR("Bashkir"),
    @XmlEnumValue("Basque")
    BASQUE("Basque"),
    @XmlEnumValue("Bengali")
    BENGALI("Bengali"),
    @XmlEnumValue("Berber")
    BERBER("Berber"),
    @XmlEnumValue("Bhutani")
    BHUTANI("Bhutani"),
    @XmlEnumValue("Bihari")
    BIHARI("Bihari"),
    @XmlEnumValue("Bislama")
    BISLAMA("Bislama"),
    @XmlEnumValue("Breton")
    BRETON("Breton"),
    @XmlEnumValue("Bulgarian")
    BULGARIAN("Bulgarian"),
    @XmlEnumValue("Burmese")
    BURMESE("Burmese"),
    @XmlEnumValue("Buryat")
    BURYAT("Buryat"),
    @XmlEnumValue("Byelorussian")
    BYELORUSSIAN("Byelorussian"),
    @XmlEnumValue("CantoneseChinese")
    CANTONESE_CHINESE("CantoneseChinese"),
    @XmlEnumValue("Castillian")
    CASTILLIAN("Castillian"),
    @XmlEnumValue("Catalan")
    CATALAN("Catalan"),
    @XmlEnumValue("Cayuga")
    CAYUGA("Cayuga"),
    @XmlEnumValue("Cheyenne")
    CHEYENNE("Cheyenne"),
    @XmlEnumValue("Chinese")
    CHINESE("Chinese"),
    @XmlEnumValue("ClassicalNewari")
    CLASSICAL_NEWARI("ClassicalNewari"),
    @XmlEnumValue("Cornish")
    CORNISH("Cornish"),
    @XmlEnumValue("Corsican")
    CORSICAN("Corsican"),
    @XmlEnumValue("Creole")
    CREOLE("Creole"),
    @XmlEnumValue("CrimeanTatar")
    CRIMEAN_TATAR("CrimeanTatar"),
    @XmlEnumValue("Croatian")
    CROATIAN("Croatian"),
    @XmlEnumValue("Czech")
    CZECH("Czech"),
    @XmlEnumValue("Danish")
    DANISH("Danish"),
    @XmlEnumValue("Dargwa")
    DARGWA("Dargwa"),
    @XmlEnumValue("Dutch")
    DUTCH("Dutch"),
    @XmlEnumValue("English")
    ENGLISH("English"),
    @XmlEnumValue("Esperanto")
    ESPERANTO("Esperanto"),
    @XmlEnumValue("Estonian")
    ESTONIAN("Estonian"),
    @XmlEnumValue("Faroese")
    FAROESE("Faroese"),
    @XmlEnumValue("Farsi")
    FARSI("Farsi"),
    @XmlEnumValue("Fiji")
    FIJI("Fiji"),
    @XmlEnumValue("Filipino")
    FILIPINO("Filipino"),
    @XmlEnumValue("Finnish")
    FINNISH("Finnish"),
    @XmlEnumValue("Flemish")
    FLEMISH("Flemish"),
    @XmlEnumValue("French")
    FRENCH("French"),
    @XmlEnumValue("FrenchCanadian")
    FRENCH_CANADIAN("FrenchCanadian"),
    @XmlEnumValue("Frisian")
    FRISIAN("Frisian"),
    @XmlEnumValue("Galician")
    GALICIAN("Galician"),
    @XmlEnumValue("Georgian")
    GEORGIAN("Georgian"),
    @XmlEnumValue("German")
    GERMAN("German"),
    @XmlEnumValue("Gibberish")
    GIBBERISH("Gibberish"),
    @XmlEnumValue("Greek")
    GREEK("Greek"),
    @XmlEnumValue("Greenlandic")
    GREENLANDIC("Greenlandic"),
    @XmlEnumValue("Guarani")
    GUARANI("Guarani"),
    @XmlEnumValue("Gujarati")
    GUJARATI("Gujarati"),
    @XmlEnumValue("Gullah")
    GULLAH("Gullah"),
    @XmlEnumValue("Hausa")
    HAUSA("Hausa"),
    @XmlEnumValue("Hawaiian")
    HAWAIIAN("Hawaiian"),
    @XmlEnumValue("Hebrew")
    HEBREW("Hebrew"),
    @XmlEnumValue("Hindi")
    HINDI("Hindi"),
    @XmlEnumValue("Hmong")
    HMONG("Hmong"),
    @XmlEnumValue("Hungarian")
    HUNGARIAN("Hungarian"),
    @XmlEnumValue("Icelandic")
    ICELANDIC("Icelandic"),
    @XmlEnumValue("IndoEuropean")
    INDO_EUROPEAN("IndoEuropean"),
    @XmlEnumValue("Indonesian")
    INDONESIAN("Indonesian"),
    @XmlEnumValue("Ingush")
    INGUSH("Ingush"),
    @XmlEnumValue("Interlingua")
    INTERLINGUA("Interlingua"),
    @XmlEnumValue("Interlingue")
    INTERLINGUE("Interlingue"),
    @XmlEnumValue("Inuktitun")
    INUKTITUN("Inuktitun"),
    @XmlEnumValue("Inuktitut")
    INUKTITUT("Inuktitut"),
    @XmlEnumValue("Inupiak")
    INUPIAK("Inupiak"),
    @XmlEnumValue("Inupiaq")
    INUPIAQ("Inupiaq"),
    @XmlEnumValue("Irish")
    IRISH("Irish"),
    @XmlEnumValue("Italian")
    ITALIAN("Italian"),
    @XmlEnumValue("Japanese")
    JAPANESE("Japanese"),
    @XmlEnumValue("Javanese")
    JAVANESE("Javanese"),
    @XmlEnumValue("Kalaallisut")
    KALAALLISUT("Kalaallisut"),
    @XmlEnumValue("Kalmyk")
    KALMYK("Kalmyk"),
    @XmlEnumValue("Kannada")
    KANNADA("Kannada"),
    @XmlEnumValue("KarachayBalkar")
    KARACHAY_BALKAR("KarachayBalkar"),
    @XmlEnumValue("Kashmiri")
    KASHMIRI("Kashmiri"),
    @XmlEnumValue("Kashubian")
    KASHUBIAN("Kashubian"),
    @XmlEnumValue("Kazakh")
    KAZAKH("Kazakh"),
    @XmlEnumValue("Khmer")
    KHMER("Khmer"),
    @XmlEnumValue("Kinyarwanda")
    KINYARWANDA("Kinyarwanda"),
    @XmlEnumValue("Kirghiz")
    KIRGHIZ("Kirghiz"),
    @XmlEnumValue("Kirundi")
    KIRUNDI("Kirundi"),
    @XmlEnumValue("Klingon")
    KLINGON("Klingon"),
    @XmlEnumValue("Korean")
    KOREAN("Korean"),
    @XmlEnumValue("Kurdish")
    KURDISH("Kurdish"),
    @XmlEnumValue("Ladino")
    LADINO("Ladino"),
    @XmlEnumValue("Lao")
    LAO("Lao"),
    @XmlEnumValue("Lapp")
    LAPP("Lapp"),
    @XmlEnumValue("Latin")
    LATIN("Latin"),
    @XmlEnumValue("Latvian")
    LATVIAN("Latvian"),
    @XmlEnumValue("Lingala")
    LINGALA("Lingala"),
    @XmlEnumValue("Lithuanian")
    LITHUANIAN("Lithuanian"),
    @XmlEnumValue("Lojban")
    LOJBAN("Lojban"),
    @XmlEnumValue("LowerSorbian")
    LOWER_SORBIAN("LowerSorbian"),
    @XmlEnumValue("Macedonian")
    MACEDONIAN("Macedonian"),
    @XmlEnumValue("Malagasy")
    MALAGASY("Malagasy"),
    @XmlEnumValue("Malay")
    MALAY("Malay"),
    @XmlEnumValue("Malayalam")
    MALAYALAM("Malayalam"),
    @XmlEnumValue("Maltese")
    MALTESE("Maltese"),
    @XmlEnumValue("MandarinChinese")
    MANDARIN_CHINESE("MandarinChinese"),
    @XmlEnumValue("Maori")
    MAORI("Maori"),
    @XmlEnumValue("Marathi")
    MARATHI("Marathi"),
    @XmlEnumValue("Mende")
    MENDE("Mende"),
    @XmlEnumValue("MiddleEnglish")
    MIDDLE_ENGLISH("MiddleEnglish"),
    @XmlEnumValue("Mirandese")
    MIRANDESE("Mirandese"),
    @XmlEnumValue("Moksha")
    MOKSHA("Moksha"),
    @XmlEnumValue("Moldavian")
    MOLDAVIAN("Moldavian"),
    @XmlEnumValue("Mongo")
    MONGO("Mongo"),
    @XmlEnumValue("Mongolian")
    MONGOLIAN("Mongolian"),
    @XmlEnumValue("Multilingual")
    MULTILINGUAL("Multilingual"),
    @XmlEnumValue("Nauru")
    NAURU("Nauru"),
    @XmlEnumValue("Navaho")
    NAVAHO("Navaho"),
    @XmlEnumValue("Nepali")
    NEPALI("Nepali"),
    @XmlEnumValue("Nogai")
    NOGAI("Nogai"),
    @XmlEnumValue("Norwegian")
    NORWEGIAN("Norwegian"),
    @XmlEnumValue("Occitan")
    OCCITAN("Occitan"),
    @XmlEnumValue("OldEnglish")
    OLD_ENGLISH("OldEnglish"),
    @XmlEnumValue("Oriya")
    ORIYA("Oriya"),
    @XmlEnumValue("Oromo")
    OROMO("Oromo"),
    @XmlEnumValue("Pashto")
    PASHTO("Pashto"),
    @XmlEnumValue("Persian")
    PERSIAN("Persian"),
    @XmlEnumValue("PigLatin")
    PIG_LATIN("PigLatin"),
    @XmlEnumValue("Polish")
    POLISH("Polish"),
    @XmlEnumValue("Portuguese")
    PORTUGUESE("Portuguese"),
    @XmlEnumValue("Punjabi")
    PUNJABI("Punjabi"),
    @XmlEnumValue("Quechua")
    QUECHUA("Quechua"),
    @XmlEnumValue("Romance")
    ROMANCE("Romance"),
    @XmlEnumValue("Romanian")
    ROMANIAN("Romanian"),
    @XmlEnumValue("Romany")
    ROMANY("Romany"),
    @XmlEnumValue("Russian")
    RUSSIAN("Russian"),
    @XmlEnumValue("Samaritan")
    SAMARITAN("Samaritan"),
    @XmlEnumValue("Samoan")
    SAMOAN("Samoan"),
    @XmlEnumValue("Sangho")
    SANGHO("Sangho"),
    @XmlEnumValue("Sanskrit")
    SANSKRIT("Sanskrit"),
    @XmlEnumValue("Serbian")
    SERBIAN("Serbian"),
    @XmlEnumValue("Serbo-Croatian")
    SERBO_CROATIAN("Serbo-Croatian"),
    @XmlEnumValue("Sesotho")
    SESOTHO("Sesotho"),
    @XmlEnumValue("Setswana")
    SETSWANA("Setswana"),
    @XmlEnumValue("Shona")
    SHONA("Shona"),
    @XmlEnumValue("SichuanYi")
    SICHUAN_YI("SichuanYi"),
    @XmlEnumValue("Sicilian")
    SICILIAN("Sicilian"),
    @XmlEnumValue("SignLanguage")
    SIGN_LANGUAGE("SignLanguage"),
    @XmlEnumValue("Sindhi")
    SINDHI("Sindhi"),
    @XmlEnumValue("Sinhalese")
    SINHALESE("Sinhalese"),
    @XmlEnumValue("Siswati")
    SISWATI("Siswati"),
    @XmlEnumValue("Slavic")
    SLAVIC("Slavic"),
    @XmlEnumValue("Slovak")
    SLOVAK("Slovak"),
    @XmlEnumValue("Slovakian")
    SLOVAKIAN("Slovakian"),
    @XmlEnumValue("Slovene")
    SLOVENE("Slovene"),
    @XmlEnumValue("Somali")
    SOMALI("Somali"),
    @XmlEnumValue("Spanish")
    SPANISH("Spanish"),
    @XmlEnumValue("Sumerian")
    SUMERIAN("Sumerian"),
    @XmlEnumValue("Sundanese")
    SUNDANESE("Sundanese"),
    @XmlEnumValue("Swahili")
    SWAHILI("Swahili"),
    @XmlEnumValue("Swedish")
    SWEDISH("Swedish"),
    @XmlEnumValue("SwissGerman")
    SWISS_GERMAN("SwissGerman"),
    @XmlEnumValue("Syriac")
    SYRIAC("Syriac"),
    @XmlEnumValue("Tagalog")
    TAGALOG("Tagalog"),
    @XmlEnumValue("TaiwaneseChinese")
    TAIWANESE_CHINESE("TaiwaneseChinese"),
    @XmlEnumValue("Tajik")
    TAJIK("Tajik"),
    @XmlEnumValue("Tamil")
    TAMIL("Tamil"),
    @XmlEnumValue("Tatar")
    TATAR("Tatar"),
    @XmlEnumValue("Telugu")
    TELUGU("Telugu"),
    @XmlEnumValue("Thai")
    THAI("Thai"),
    @XmlEnumValue("Tibetan")
    TIBETAN("Tibetan"),
    @XmlEnumValue("Tigrinya")
    TIGRINYA("Tigrinya"),
    @XmlEnumValue("Tonga")
    TONGA("Tonga"),
    @XmlEnumValue("Tsonga")
    TSONGA("Tsonga"),
    @XmlEnumValue("Turkish")
    TURKISH("Turkish"),
    @XmlEnumValue("Turkmen")
    TURKMEN("Turkmen"),
    @XmlEnumValue("Twi")
    TWI("Twi"),
    @XmlEnumValue("Udmurt")
    UDMURT("Udmurt"),
    @XmlEnumValue("Uighur")
    UIGHUR("Uighur"),
    @XmlEnumValue("Ukrainian")
    UKRAINIAN("Ukrainian"),
    @XmlEnumValue("Ukranian")
    UKRANIAN("Ukranian"),
    @XmlEnumValue("Unknown")
    UNKNOWN("Unknown"),
    @XmlEnumValue("Urdu")
    URDU("Urdu"),
    @XmlEnumValue("Uzbek")
    UZBEK("Uzbek"),
    @XmlEnumValue("Vietnamese")
    VIETNAMESE("Vietnamese"),
    @XmlEnumValue("Volapuk")
    VOLAPUK("Volapuk"),
    @XmlEnumValue("Welsh")
    WELSH("Welsh"),
    @XmlEnumValue("Wolof")
    WOLOF("Wolof"),
    @XmlEnumValue("Xhosa")
    XHOSA("Xhosa"),
    @XmlEnumValue("Yiddish")
    YIDDISH("Yiddish"),
    @XmlEnumValue("Yoruba")
    YORUBA("Yoruba"),
    @XmlEnumValue("Zhuang")
    ZHUANG("Zhuang"),
    @XmlEnumValue("Zulu")
    ZULU("Zulu");
    private final String value;

    LanguageStringType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static LanguageStringType fromValue(String v) {
        for (LanguageStringType c: LanguageStringType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
