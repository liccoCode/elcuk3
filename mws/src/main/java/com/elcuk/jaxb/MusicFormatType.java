
package com.elcuk.jaxb;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for MusicFormatType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="MusicFormatType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="authorized_bootleg"/>
 *     &lt;enumeration value="bsides"/>
 *     &lt;enumeration value="best_of"/>
 *     &lt;enumeration value="box_set"/>
 *     &lt;enumeration value="original_recording"/>
 *     &lt;enumeration value="reissued"/>
 *     &lt;enumeration value="remastered"/>
 *     &lt;enumeration value="soundtrack"/>
 *     &lt;enumeration value="special_edition"/>
 *     &lt;enumeration value="special_limited_edition"/>
 *     &lt;enumeration value="cast_recording"/>
 *     &lt;enumeration value="compilation"/>
 *     &lt;enumeration value="deluxe_edition"/>
 *     &lt;enumeration value="digital_sound"/>
 *     &lt;enumeration value="double_lp"/>
 *     &lt;enumeration value="explicit_lyrics"/>
 *     &lt;enumeration value="hi-fidelity"/>
 *     &lt;enumeration value="import"/>
 *     &lt;enumeration value="limited_collectors_edition"/>
 *     &lt;enumeration value="limited_edition"/>
 *     &lt;enumeration value="remixes"/>
 *     &lt;enumeration value="live"/>
 *     &lt;enumeration value="extra_tracks"/>
 *     &lt;enumeration value="cutout"/>
 *     &lt;enumeration value="cd_and_dvd"/>
 *     &lt;enumeration value="dual_disc"/>
 *     &lt;enumeration value="hybrid_sacd"/>
 *     &lt;enumeration value="sacd"/>
 *     &lt;enumeration value="minidisc"/>
 *     &lt;enumeration value="uk_import"/>
 *     &lt;enumeration value="us_import"/>
 *     &lt;enumeration value="jp_import"/>
 *     &lt;enumeration value="copy_protected_cd"/>
 *     &lt;enumeration value="double_lp"/>
 *     &lt;enumeration value="soundtrack"/>
 *     &lt;enumeration value="cd-single"/>
 *     &lt;enumeration value="remastered"/>
 *     &lt;enumeration value="box_set"/>
 *     &lt;enumeration value="double_cd"/>
 *     &lt;enumeration value="karaoke"/>
 *     &lt;enumeration value="limited_edition"/>
 *     &lt;enumeration value="maxi_single"/>
 *     &lt;enumeration value="mp3_audio"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "MusicFormatType")
@XmlEnum
public enum MusicFormatType {

    @XmlEnumValue("authorized_bootleg")
    AUTHORIZED_BOOTLEG("authorized_bootleg"),
    @XmlEnumValue("bsides")
    BSIDES("bsides"),
    @XmlEnumValue("best_of")
    BEST_OF("best_of"),
    @XmlEnumValue("box_set")
    BOX_SET("box_set"),
    @XmlEnumValue("original_recording")
    ORIGINAL_RECORDING("original_recording"),
    @XmlEnumValue("reissued")
    REISSUED("reissued"),
    @XmlEnumValue("remastered")
    REMASTERED("remastered"),
    @XmlEnumValue("soundtrack")
    SOUNDTRACK("soundtrack"),
    @XmlEnumValue("special_edition")
    SPECIAL_EDITION("special_edition"),
    @XmlEnumValue("special_limited_edition")
    SPECIAL_LIMITED_EDITION("special_limited_edition"),
    @XmlEnumValue("cast_recording")
    CAST_RECORDING("cast_recording"),
    @XmlEnumValue("compilation")
    COMPILATION("compilation"),
    @XmlEnumValue("deluxe_edition")
    DELUXE_EDITION("deluxe_edition"),
    @XmlEnumValue("digital_sound")
    DIGITAL_SOUND("digital_sound"),
    @XmlEnumValue("double_lp")
    DOUBLE_LP("double_lp"),
    @XmlEnumValue("explicit_lyrics")
    EXPLICIT_LYRICS("explicit_lyrics"),
    @XmlEnumValue("hi-fidelity")
    HI_FIDELITY("hi-fidelity"),
    @XmlEnumValue("import")
    IMPORT("import"),
    @XmlEnumValue("limited_collectors_edition")
    LIMITED_COLLECTORS_EDITION("limited_collectors_edition"),
    @XmlEnumValue("limited_edition")
    LIMITED_EDITION("limited_edition"),
    @XmlEnumValue("remixes")
    REMIXES("remixes"),
    @XmlEnumValue("live")
    LIVE("live"),
    @XmlEnumValue("extra_tracks")
    EXTRA_TRACKS("extra_tracks"),
    @XmlEnumValue("cutout")
    CUTOUT("cutout"),
    @XmlEnumValue("cd_and_dvd")
    CD_AND_DVD("cd_and_dvd"),
    @XmlEnumValue("dual_disc")
    DUAL_DISC("dual_disc"),
    @XmlEnumValue("hybrid_sacd")
    HYBRID_SACD("hybrid_sacd"),
    @XmlEnumValue("sacd")
    SACD("sacd"),
    @XmlEnumValue("minidisc")
    MINIDISC("minidisc"),
    @XmlEnumValue("uk_import")
    UK_IMPORT("uk_import"),
    @XmlEnumValue("us_import")
    US_IMPORT("us_import"),
    @XmlEnumValue("jp_import")
    JP_IMPORT("jp_import"),
    @XmlEnumValue("copy_protected_cd")
    COPY_PROTECTED_CD("copy_protected_cd"),
    @XmlEnumValue("cd-single")
    CD_SINGLE("cd-single"),
    @XmlEnumValue("double_cd")
    DOUBLE_CD("double_cd"),
    @XmlEnumValue("karaoke")
    KARAOKE("karaoke"),
    @XmlEnumValue("maxi_single")
    MAXI_SINGLE("maxi_single"),
    @XmlEnumValue("mp3_audio")
    MP_3_AUDIO("mp3_audio");
    private final String value;

    MusicFormatType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static MusicFormatType fromValue(String v) {
        for (MusicFormatType c: MusicFormatType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
