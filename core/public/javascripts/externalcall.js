window.YQ = window.YQ || {}, YQ.configs = YQ.configs || {}, YQ.configs.browserCheck = !1, function (e, t) {
  "use strict";
  var i = "model", o = "name", r = "type", n = "vendor", s = "version", a = "architecture", l = "console", d = "mobile",
  c = "tablet", u = "smarttv", w = function (e, t) {
    var i = {};
    for(var o in e) t[o] && t[o].length % 2 == 0 ? i[o] = t[o].concat(e[o]) : i[o] = e[o];
    return i
  }, m = function (e, t) {
    return "string" == typeof e && -1 !== t.toLowerCase().indexOf(e.toLowerCase())
  }, p = function (e) {
    return e.toLowerCase()
  }, f = function (e) {
    return "string" == typeof e ? e.replace(/[^\d\.]/g, "").split(".")[0] : void 0
  }, h = function (e) {
    return e.replace(/^[\s\uFEFF\xA0]+|[\s\uFEFF\xA0]+$/g, "")
  }, b = {
    rgx: function (e, t) {
      for(var i, o, r, n, s, a, l = 0; l < t.length && !s;) {
        var d = t[l], c = t[l + 1];
        for(i = o = 0; i < d.length && !s;) if (s = d[i++].exec(e)) for(r = 0; r < c.length; r++) a = s[++o], "object" == typeof(n = c[r]) && n.length > 0 ? 2 == n.length ? "function" == typeof n[1] ? this[n[0]] = n[1].call(this, a) : this[n[0]] = n[1] : 3 == n.length ? "function" != typeof n[1] || n[1].exec && n[1].test ? this[n[0]] = a ? a.replace(n[1], n[2]) : void 0 : this[n[0]] = a ? n[1].call(this, a, n[2]) : void 0 : 4 == n.length && (this[n[0]] = a ? n[3].call(this, a.replace(n[1], n[2])) : void 0) : this[n] = a || void 0;
        l += 2
      }
    },
    str: function (e, t) {
      for(var i in t) if ("object" == typeof t[i] && t[i].length > 0) {
        for(var o = 0; o < t[i].length; o++) if (m(t[i][o], e)) return "?" === i ? void 0 : i
      } else if (m(t[i], e)) return "?" === i ? void 0 : i;
      return e
    }
  }, g = {
    oldsafari: {
      version: {
        "1.0": "/8",
        1.2: "/1",
        1.3: "/3",
        "2.0": "/412",
        "2.0.2": "/416",
        "2.0.3": "/417",
        "2.0.4": "/419",
        "?": "/"
      }
    }
  }, _ = {
    amazon: {model: {"Fire Phone": ["SD", "KF"]}},
    sprint: {
      model: {"Evo Shift 4G": "7373KT"},
      vendor: {
        HTC: "APA",
        Sprint: "Sprint"
      }
    }
  }, v = {
    windows: {
      version: {
        ME: "4.90",
        "NT 3.11": "NT3.51",
        "NT 4.0": "NT4.0",
        2000: "NT 5.0",
        XP: ["NT 5.1", "NT 5.2"],
        Vista: "NT 6.0",
        7: "NT 6.1",
        8: "NT 6.2",
        8.1: "NT 6.3",
        10: ["NT 6.4", "NT 10.0"],
        RT: "ARM"
      }
    }
  }, y = {
    browser: [[/(opera\smini)\/([\w\.-]+)/i, /(opera\s[mobiletab]+).+version\/([\w\.-]+)/i, /(opera).+version\/([\w\.]+)/i, /(opera)[\/\s]+([\w\.]+)/i], [o, s], [/(opios)[\/\s]+([\w\.]+)/i], [[o, "Opera Mini"], s], [/\s(opr)\/([\w\.]+)/i], [[o, "Opera"], s], [/(kindle)\/([\w\.]+)/i, /(lunascape|maxthon|netfront|jasmine|blazer)[\/\s]?([\w\.]+)*/i, /(avant\s|iemobile|slim|baidu)(?:browser)?[\/\s]?([\w\.]*)/i, /(?:ms|\()(ie)\s([\w\.]+)/i, /(rekonq)\/([\w\.]+)*/i, /(chromium|flock|rockmelt|midori|epiphany|silk|skyfire|ovibrowser|bolt|iron|vivaldi|iridium|phantomjs|bowser)\/([\w\.-]+)/i], [o, s], [/(trident).+rv[:\s]([\w\.]+).+like\sgecko/i], [[o, "IE"], s], [/(edge)\/((\d+)?[\w\.]+)/i], [o, s], [/(yabrowser)\/([\w\.]+)/i], [[o, "Yandex"], s], [/(puffin)\/([\w\.]+)/i], [[o, "Puffin"], s], [/(uc\s?browser)[\/\s]?([\w\.]+)/i, /ucweb.+(ucbrowser)[\/\s]?([\w\.]+)/i, /juc.+(ucweb)[\/\s]?([\w\.]+)/i, /(ucbrowser)\/([\w\.]+)/i], [[o, "UCBrowser"], s], [/(comodo_dragon)\/([\w\.]+)/i], [[o, /_/g, " "], s], [/(micromessenger)\/([\w\.]+)/i], [[o, "WeChat"], s], [/m?(qqbrowser)[\/\s]?([\w\.]+)/i], [o, s], [/xiaomi\/miuibrowser\/([\w\.]+)/i], [s, [o, "MIUI Browser"]], [/;fbav\/([\w\.]+);/i], [s, [o, "Facebook"]], [/(headlesschrome) ([\w\.]+)/i], [s, [o, "Chrome Headless"]], [/\swv\).+(chrome)\/([\w\.]+)/i], [[o, /(.+)/, "$1 WebView"], s], [/android.+samsungbrowser\/([\w\.]+)/i, /android.+version\/([\w\.]+)\s+(?:mobile\s?safari|safari)*/i], [s, [o, "Android Browser"]], [/(chrome|omniweb|arora|[tizenoka]{5}\s?browser)\/v?([\w\.]+)/i], [o, s], [/(dolfin)\/([\w\.]+)/i], [[o, "Dolphin"], s], [/((?:android.+)crmo|crios)\/([\w\.]+)/i], [[o, "Chrome"], s], [/(coast)\/([\w\.]+)/i], [[o, "Opera Coast"], s], [/fxios\/([\w\.-]+)/i], [s, [o, "Firefox"]], [/version\/([\w\.]+).+?mobile\/\w+\s(safari)/i], [s, [o, "Mobile Safari"]], [/version\/([\w\.]+).+?(mobile\s?safari|safari)/i], [s, o], [/webkit.+?(mobile\s?safari|safari)(\/[\w\.]+)/i], [o, [s, b.str, g.oldsafari.version]], [/(konqueror)\/([\w\.]+)/i, /(webkit|khtml)\/([\w\.]+)/i], [o, s], [/(navigator|netscape)\/([\w\.-]+)/i], [[o, "Netscape"], s], [/(swiftfox)/i, /(icedragon|iceweasel|camino|chimera|fennec|maemo\sbrowser|minimo|conkeror)[\/\s]?([\w\.\+]+)/i, /(firefox|seamonkey|k-meleon|icecat|iceape|firebird|phoenix)\/([\w\.-]+)/i, /(mozilla)\/([\w\.]+).+rv\:.+gecko\/\d+/i, /(polaris|lynx|dillo|icab|doris|amaya|w3m|netsurf|sleipnir)[\/\s]?([\w\.]+)/i, /(links)\s\(([\w\.]+)/i, /(gobrowser)\/?([\w\.]+)*/i, /(ice\s?browser)\/v?([\w\._]+)/i, /(mosaic)[\/\s]([\w\.]+)/i], [o, s]],
    cpu: [[/(?:(amd|x(?:(?:86|64)[_-])?|wow|win)64)[;\)]/i], [[a, "amd64"]], [/(ia32(?=;))/i], [[a, p]], [/((?:i[346]|x)86)[;\)]/i], [[a, "ia32"]], [/windows\s(ce|mobile);\sppc;/i], [[a, "arm"]], [/((?:ppc|powerpc)(?:64)?)(?:\smac|;|\))/i], [[a, /ower/, "", p]], [/(sun4\w)[;\)]/i], [[a, "sparc"]], [/((?:avr32|ia64(?=;))|68k(?=\))|arm(?:64|(?=v\d+;))|(?=atmel\s)avr|(?:irix|mips|sparc)(?:64)?(?=;)|pa-risc)/i], [[a, p]]],
    device: [[/\((ipad|playbook);[\w\s\);-]+(rim|apple)/i], [i, n, [r, c]], [/applecoremedia\/[\w\.]+ \((ipad)/], [i, [n, "Apple"], [r, c]], [/(apple\s{0,1}tv)/i], [[i, "Apple TV"], [n, "Apple"]], [/(archos)\s(gamepad2?)/i, /(hp).+(touchpad)/i, /(hp).+(tablet)/i, /(kindle)\/([\w\.]+)/i, /\s(nook)[\w\s]+build\/(\w+)/i, /(dell)\s(strea[kpr\s\d]*[\dko])/i], [n, i, [r, c]], [/(kf[A-z]+)\sbuild\/[\w\.]+.*silk\//i], [i, [n, "Amazon"], [r, c]], [/(sd|kf)[0349hijorstuw]+\sbuild\/[\w\.]+.*silk\//i], [[i, b.str, _.amazon.model], [n, "Amazon"], [r, d]], [/\((ip[honed|\s\w*]+);.+(apple)/i], [i, n, [r, d]], [/\((ip[honed|\s\w*]+);/i], [i, [n, "Apple"], [r, d]], [/(blackberry)[\s-]?(\w+)/i, /(blackberry|benq|palm(?=\-)|sonyericsson|acer|asus|dell|huawei|meizu|motorola|polytron)[\s_-]?([\w-]+)*/i, /(hp)\s([\w\s]+\w)/i, /(asus)-?(\w+)/i], [n, i, [r, d]], [/\(bb10;\s(\w+)/i], [i, [n, "BlackBerry"], [r, d]], [/android.+(transfo[prime\s]{4,10}\s\w+|eeepc|slider\s\w+|nexus 7|padfone)/i], [i, [n, "Asus"], [r, c]], [/(sony)\s(tablet\s[ps])\sbuild\//i, /(sony)?(?:sgp.+)\sbuild\//i], [[n, "Sony"], [i, "Xperia Tablet"], [r, c]], [/(?:sony)?(?:(?:(?:c|d)\d{4})|(?:so[-l].+))\sbuild\//i], [[n, "Sony"], [i, "Xperia Phone"], [r, d]], [/\s(ouya)\s/i, /(nintendo)\s([wids3u]+)/i], [n, i, [r, l]], [/android.+;\s(shield)\sbuild/i], [i, [n, "Nvidia"], [r, l]], [/(playstation\s[34portablevi]+)/i], [i, [n, "Sony"], [r, l]], [/(sprint\s(\w+))/i], [[n, b.str, _.sprint.vendor], [i, b.str, _.sprint.model], [r, d]], [/(lenovo)\s?(S(?:5000|6000)+(?:[-][\w+]))/i], [n, i, [r, c]], [/(htc)[;_\s-]+([\w\s]+(?=\))|\w+)*/i, /(zte)-(\w+)*/i, /(alcatel|geeksphone|huawei|lenovo|nexian|panasonic|(?=;\s)sony)[_\s-]?([\w-]+)*/i], [n, [i, /_/g, " "], [r, d]], [/(nexus\s9)/i], [i, [n, "HTC"], [r, c]], [/(nexus\s6p)/i], [i, [n, "Huawei"], [r, d]], [/(microsoft);\s(lumia[\s\w]+)/i], [n, i, [r, d]], [/[\s\(;](xbox(?:\sone)?)[\s\);]/i], [i, [n, "Microsoft"], [r, l]], [/(kin\.[onetw]{3})/i], [[i, /\./g, " "], [n, "Microsoft"], [r, d]], [/\s(milestone|droid(?:[2-4x]|\s(?:bionic|x2|pro|razr))?(:?\s4g)?)[\w\s]+build\//i, /mot[\s-]?(\w+)*/i, /(XT\d{3,4}) build\//i, /(nexus\s6)/i], [i, [n, "Motorola"], [r, d]], [/android.+\s(mz60\d|xoom[\s2]{0,2})\sbuild\//i], [i, [n, "Motorola"], [r, c]], [/hbbtv\/\d+\.\d+\.\d+\s+\([\w\s]*;\s*(\w[^;]*);([^;]*)/i], [[n, h], [i, h], [r, u]], [/hbbtv.+maple;(\d+)/i], [[i, /^/, "SmartTV"], [n, "Samsung"], [r, u]], [/\(dtv[\);].+(aquos)/i], [i, [n, "Sharp"], [r, u]], [/android.+((sch-i[89]0\d|shw-m380s|gt-p\d{4}|gt-n\d+|sgh-t8[56]9|nexus 10))/i, /((SM-T\w+))/i], [[n, "Samsung"], i, [r, c]], [/smart-tv.+(samsung)/i], [n, [r, u], i], [/((s[cgp]h-\w+|gt-\w+|galaxy\snexus|sm-\w[\w\d]+))/i, /(sam[sung]*)[\s-]*(\w+-?[\w-]*)*/i, /sec-((sgh\w+))/i], [[n, "Samsung"], i, [r, d]], [/sie-(\w+)*/i], [i, [n, "Siemens"], [r, d]], [/(maemo|nokia).*(n900|lumia\s\d+)/i, /(nokia)[\s_-]?([\w-]+)*/i], [[n, "Nokia"], i, [r, d]], [/android\s3\.[\s\w;-]{10}(a\d{3})/i], [i, [n, "Acer"], [r, c]], [/android\s3\.[\s\w;-]{10}(lg?)-([06cv9]{3,4})/i], [[n, "LG"], i, [r, c]], [/(lg) netcast\.tv/i], [n, i, [r, u]], [/(nexus\s[45])/i, /lg[e;\s\/-]+(\w+)*/i], [i, [n, "LG"], [r, d]], [/android.+(ideatab[a-z0-9\-\s]+)/i], [i, [n, "Lenovo"], [r, c]], [/linux;.+((jolla));/i], [n, i, [r, d]], [/((pebble))app\/[\d\.]+\s/i], [n, i, [r, "wearable"]], [/android.+;\s(oppo)\s?([\w\s]+)\sbuild/i], [n, i, [r, d]], [/crkey/i], [[i, "Chromecast"], [n, "Google"]], [/android.+;\s(glass)\s\d/i], [i, [n, "Google"], [r, "wearable"]], [/android.+;\s(pixel c)\s/i], [i, [n, "Google"], [r, c]], [/android.+;\s(pixel xl|pixel)\s/i], [i, [n, "Google"], [r, d]], [/android.+(\w+)\s+build\/hm\1/i, /android.+(hm[\s\-_]*note?[\s_]*(?:\d\w)?)\s+build/i, /android.+(mi[\s\-_]*(?:one|one[\s_]plus|note lte)?[\s_]*(?:\d\w)?)\s+build/i], [[i, /_/g, " "], [n, "Xiaomi"], [r, d]], [/android.+a000(1)\s+build/i], [i, [n, "OnePlus"], [r, d]], [/\s(tablet)[;\/]/i, /\s(mobile)(?:[;\/]|\ssafari)/i], [[r, p], n, i]],
    engine: [[/windows.+\sedge\/([\w\.]+)/i], [s, [o, "EdgeHTML"]], [/(presto)\/([\w\.]+)/i, /(webkit|trident|netfront|netsurf|amaya|lynx|w3m)\/([\w\.]+)/i, /(khtml|tasman|links)[\/\s]\(?([\w\.]+)/i, /(icab)[\/\s]([23]\.[\d\.]+)/i], [o, s], [/rv\:([\w\.]+).*(gecko)/i], [s, o]],
    os: [[/microsoft\s(windows)\s(vista|xp)/i], [o, s], [/(windows)\snt\s6\.2;\s(arm)/i, /(windows\sphone(?:\sos)*)[\s\/]?([\d\.\s]+\w)*/i, /(windows\smobile|windows)[\s\/]?([ntce\d\.\s]+\w)/i], [o, [s, b.str, v.windows.version]], [/(win(?=3|9|n)|win\s9x\s)([nt\d\.]+)/i], [[o, "Windows"], [s, b.str, v.windows.version]], [/\((bb)(10);/i], [[o, "BlackBerry"], s], [/(blackberry)\w*\/?([\w\.]+)*/i, /(tizen)[\/\s]([\w\.]+)/i, /(android|webos|palm\sos|qnx|bada|rim\stablet\sos|meego|contiki)[\/\s-]?([\w\.]+)*/i, /linux;.+(sailfish);/i], [o, s], [/(symbian\s?os|symbos|s60(?=;))[\/\s-]?([\w\.]+)*/i], [[o, "Symbian"], s], [/\((series40);/i], [o], [/mozilla.+\(mobile;.+gecko.+firefox/i], [[o, "Firefox OS"], s], [/(nintendo|playstation)\s([wids34portablevu]+)/i, /(mint)[\/\s\(]?(\w+)*/i, /(mageia|vectorlinux)[;\s]/i, /(joli|[kxln]?ubuntu|debian|[open]*suse|gentoo|(?=\s)arch|slackware|fedora|mandriva|centos|pclinuxos|redhat|zenwalk|linpus)[\/\s-]?(?!chrom)([\w\.-]+)*/i, /(hurd|linux)\s?([\w\.]+)*/i, /(gnu)\s?([\w\.]+)*/i], [o, s], [/(cros)\s[\w]+\s([\w\.]+\w)/i], [[o, "Chromium OS"], s], [/(sunos)\s?([\w\.]+\d)*/i], [[o, "Solaris"], s], [/\s([frentopc-]{0,4}bsd|dragonfly)\s?([\w\.]+)*/i], [o, s], [/(haiku)\s(\w+)/i], [o, s], [/(ip[honead]+)(?:.*os\s([\w]+)*\slike\smac|;\sopera)/i], [[o, "iOS"], [s, /_/g, "."]], [/(mac\sos\sx)\s?([\w\s\.]+\w)*/i, /(macintosh|mac(?=_powerpc)\s)/i], [[o, "Mac OS"], [s, /_/g, "."]], [/((?:open)?solaris)[\/\s-]?([\w\.]+)*/i, /(aix)\s((\d)(?=\.|\)|\s)[\w\.]*)*/i, /(plan\s9|minix|beos|os\/2|amigaos|morphos|risc\sos|openvms)/i, /(unix)\s?([\w\.]+)*/i], [o, s]]
  }, x = function (e, t) {
    this.name = e, this[s] = t
  }, k = function (e) {
    this[a] = e
  }, E = function (e, t, i) {
    this.vendor = e, this.model = t, this.type = i
  }, I = x, S = x, C = function (t, i) {
    if (!(this instanceof C)) return new C(t, i).getResult();
    var o = t || (e && e.navigator && e.navigator.userAgent ? e.navigator.userAgent : ""), r = i ? w(y, i) : y,
    n = new x, s = new k, a = new E, l = new I, d = new S;
    return this.getBrowser = function () {
      return b.rgx.call(n, o, r.browser), n.major = f(n.version), n
    }, this.getCPU = function () {
      return b.rgx.call(s, o, r.cpu), s
    }, this.getDevice = function () {
      return b.rgx.call(a, o, r.device), a
    }, this.getEngine = function () {
      return b.rgx.call(l, o, r.engine), l
    }, this.getOS = function () {
      return b.rgx.call(d, o, r.os), d
    }, this.getResult = function () {
      return {
        ua: this.getUA(),
        browser: this.getBrowser(),
        engine: this.getEngine(),
        os: this.getOS(),
        device: this.getDevice(),
        cpu: this.getCPU()
      }
    }, this.getUA = function () {
      return o
    }, this.setUA = function (e) {
      return o = e, n = new x, s = new k, a = new E, l = new I, d = new S, this
    }, this
  };
  C.VERSION = "0.7.12", C.BROWSER = {
    NAME: o,
    MAJOR: "major",
    VERSION: s
  }, C.CPU = {ARCHITECTURE: a}, C.DEVICE = {
    MODEL: i,
    VENDOR: n,
    TYPE: r,
    CONSOLE: l,
    MOBILE: d,
    SMARTTV: u,
    TABLET: c,
    WEARABLE: "wearable",
    EMBEDDED: "embedded"
  }, C.ENGINE = {
    NAME: o,
    VERSION: s
  }, C.OS = {
    NAME: o,
    VERSION: s
  }, "undefined" != typeof exports ? ("undefined" != typeof module && module.exports && (exports = module.exports = C), exports.UAParser = C) : "function" == typeof define && define.amd ? define(function () {
    return C
  }) : e.UAParser = C;
  var T = e.jQuery || e.Zepto;
  if (void 0 !== T) {
    var Y = new C;
    T.ua = Y.getResult(), T.ua.get = function () {
      return Y.getUA()
    }, T.ua.set = function (e) {
      Y.setUA(e);
      var t = Y.getResult();
      for(var i in t) T.ua[i] = t[i]
    }
  }
}("object" == typeof window ? window : this), function (e, t) {
  e.update_i18n = {
    en: {
      __content1: "System detects it's an outdated browser. It is recommended using the following web browsers:",
      __content2: "IE10, Firefox31, Safari4, Chrome41, Opera30+ above version, or other up-to-date webkit browsers for the best possible experience.",
      __seoTitle: "Outdated Browser"
    },
    "zh-cn": {
      __content1: "你的浏览器版本过低，我们推荐你使用：",
      __content2: "IE10、Firefox31、Safari4、Chrome41、Opera30以上或其他Webkit内核浏览器，以获得最好的使用体验。",
      __seoTitle: "浏览器版本过低"
    },
    "zh-hk": {
      __content1: "你的瀏覽器版本過低，我們推薦你使用：",
      __content2: "IE10、Firefox31、Safari4、Chrome41、Opera30以上或其他webkit內核瀏覽器，以獲得最好的使用體驗。",
      __seoTitle: "流覽器版本過低"
    },
    ja: {
      __content1: "システムが古いバージョンのブラウザを検知しました。下記するウェブブラウザのご使用を推奨します：",
      __content2: "IE10, Firefox31, Safari4, Chrome41, Opera30+ 以降のバージョン又は最新の webkit ブラウザでより快適にご使用頂けます。",
      __seoTitle: "旧バージョンブラウザ"
    },
    ko: {
      __content1: "이전 버전의 브라우저로 시스템 확인이 되었습니다. 하단의 브라우저로 사용하시는 것을 권장합니다 :",
      __content2: "인터넷 익스플로러 10, 파이어 폭스 31, 사파리 4, 크롬 41, 오페라 30+ 이상의 버전, 또는 다른 최신의 웹 브라우저를 통해 최대한의 경험을 접하세요.",
      __seoTitle: "이전 버전의 브라우저"
    },
    fi: {
      __content1: "Järjestelmä on havainnut vanhentuneen selaimen. Suosittelemme käyttämään seuraavia web-selaimia:",
      __content2: "IE10, Firefox31, Safari4, Chrome41, Opera30 + -versio tai jokin muu ajan tasalla oleva web-selain, parhaan mahdollisen käyttökokemuksen takaamiseksi.",
      __seoTitle: "Vanhentunut selain"
    },
    pl: {
      __content1: "System wykrył, że korzystasz ze starej przeglądarki. Rekomendujemy korzystanie z jednej z tych przeglądarek:",
      __content2: "IE10, Firefox31, Safari4, Chrome41, Opera30+ i wersje nowsze, dla większej wygody.",
      __seoTitle: "Przestarzała przeglądarka"
    },
    tr: {
      __content1: "Sistem eski bir tarayıcı olduğunu tespit etti. Aşağıdaki web tarayıcılarını kullanmanız önerilir:",
      __content2: "IE10, Firefox31, Safari4, Chrome41, Opera30 + sürümünden veya diğer en güncel web tarayıcılarından yararlanın.",
      __seoTitle: "Eski Tarayıcı"
    },
    cs: {
      __content1: "Systém zjistil, že používáte zastaralý prohlížeč. Doporučujeme použit některý z následujících webových prohlížečů:",
      __content2: "IE10, Firefox31, Safari4, Chrome41, Opera30 a vyšší verze nebo jiné aktuální webkit prohlížeče pro nejlepší možné využití.",
      __seoTitle: "Zastaralý prohlížeč"
    },
    it: {
      __content1: "Il sistema ha rilevato un browser obsoleto. Si raccomanda l'utilizzo di uno dei seguenti browser web:",
      __content2: "Internet Explorer 10, Opera 30, Firefox 31, Chrome 41, Safari 4 o versioni più recenti. In alternativa è possibile utilizzare anche altri browser aggiornati all'ultima versione per una miglior esperienza possibile.",
      __seoTitle: "Browser obsoleto"
    },
    de: {
      __content1: "Unser System hat identifiziert, dass es einen veralteten Browser ist. Es wird empfohlen, die folgenden Browser zu verwenden:",
      __content2: "IE10, Firefox31, Safari4, Chrome41, Opera30+ und höher, oder die andere aktualisierte Webkit-Browser für die beste Erfahrung.",
      __seoTitle: "Veralteter Browser"
    },
    es: {
      __content1: "¿Está utilizando un explorador antiguo? Le recomendamos uno de estos:",
      __content2: "IE10, Firefox31, Safari4, Chrome41, Opera30 y superiores, o algún otro que esté actualizado para una mejor experiencia.",
      __seoTitle: "Explorador Antiguo"
    },
    fr: {
      __content1: "Notre système détecte que vous utilisez un navigateur obsolète. Il est recommandé d'utiliser les suivants :",
      __content2: "IE10, Firefox 31, Safari4, Chrome41, Opera30+ versions supérieures ou autres navigateurs à jours pour la meilleure expérience possible.",
      __seoTitle: "Navigateur Obsolète"
    },
    ru: {
      __content1: "Система обнаружила устаревший браузер. Рекомендуется использовать обновленные браузеры:",
      __content2: "IE10, Firefox31, Safari4, Chrome41, Opera30 +  или выше версии и другие.",
      __seoTitle: "Устаревший браузер"
    },
    pt: {
      __content1: "O sistema detecta que é um navegador desatualizado. Recomenda-se usar os seguintes navegadores:",
      __content2: "IE10, Firefox31, Safari4, Chrome41, Opera30+ e versões superiores, ou outros navegadores (webkits) atualizados para obter a melhor experiência possível.",
      __seoTitle: "Navegador Desatualizado"
    },
    nl: {
      __content1: "Systeem heeft een verouderde browser gedetecteerd. Het is aangeraden een van de volgende browsers te gebruiken:",
      __content2: "IE10, Firefox31, Safari4, Chrome41, Opera30+ of nieuwere versie, of andere up-to-date webkit browsers voor de beste ervaring.",
      __seoTitle: "Verouderde Browser"
    },
    uk: {
      __content1: "System detects it's an outdated browser. It is recommended using the following web browsers:",
      __content2: "IE10, Firefox31, Safari4, Chrome41, Opera30+ above version, or other up-to-date webkit browsers for the best possible experience.",
      __seoTitle: "Outdated Browser"
    },
    hu: {
      __content1: "A rendszer jelzése szerint elavult veziójú böngészőt használ. Az alábbi böngészőt ajánljuk:",
      __content2: "IE10, Firefox31, Safari4, Chrome41, Opera30+ vagy frissebb verziót, illetve egyéb naprakész webkit böngészőt, mely a legjobb böngészési élményt garantálja.",
      __seoTitle: "Elavult böngésző"
    },
    sv: {
      __content1: "Systemet har detekterat att du använder en gammal webbläsare. Följande webbläsare rekommenderas:",
      __content2: "IE10, Firefox31, Safari4, Chrome41, Opera30+ eller högre; alternativt kan du använda någon annan ny webkit-baserad webbläsare.",
      __seoTitle: "Gammal webbläsare"
    },
    kk: {
      __content1: "System detects it's an outdated browser. It is recommended using the following web browsers:",
      __content2: "IE10, Firefox31, Safari4, Chrome41, Opera30+ above version, or other up-to-date webkit browsers for the best possible experience.",
      __seoTitle: "Outdated Browser"
    },
    el: {
      __content1: "Το σύστημα εντοπίζει ότι το πρόγραμμα περιήγησης είναι παλιό. Συνιστάται να χρησιμοποιείτε τα ακόλουθα προγράμματα περιήγησης:",
      __content2: "IE10, Firefox31, Safari4, Chrome41, Opera30+ παραπάνω έκδοση ή άλλα ενημερωμένα προγράμματα περιήγησης webkit για την καλύτερη δυνατή εμπειρία.",
      __seoTitle: "Μη ενημερωμένο πρόγραμμα περιήγησης"
    },
    th: {
      __content1: "System detects it's an outdated browser. It is recommended using the following web browsers:",
      __content2: "IE10, Firefox31, Safari4, Chrome41, Opera30+ above version, or other up-to-date webkit browsers for the best possible experience.",
      __seoTitle: "Outdated Browser"
    },
    bg: {
      __content1: "System detects it's an outdated browser. It is recommended using the following web browsers:",
      __content2: "IE10, Firefox31, Safari4, Chrome41, Opera30+ above version, or other up-to-date webkit browsers for the best possible experience.",
      __seoTitle: "Outdated Browser"
    },
    sk: {
      __content1: "Náš systém zistil, ze používate zastaralý webový prehliadač. Odporúčame použiť niektorý z následujúcich webových prehliadačov:",
      __content2: "IE10, Firefox31, Safari4, Chrome41, Opera30+ a vyššie verzie alebo iný aktuálny webkit prehliadač pre čo najlepší zážitok z používania.",
      __seoTitle: "Zastaralý prehliadač"
    },
    lt: {
      __content1: "System detects it's an outdated browser. It is recommended using the following web browsers:",
      __content2: "IE10, Firefox31, Safari4, Chrome41, Opera30+ above version, or other up-to-date webkit browsers for the best possible experience.",
      __seoTitle: "Outdated Browser"
    },
    ro: {
      __content1: "Sistemul a detectat un browser invechit. Este recomandat sa utilizezi urmatoarele browsere:",
      __content2: "IE10, Firefox31, Safari4, Chrome41, Opera30+ sau mai noi, sau alte browsere actualizate pentru o mai buna experienta de utilizare.",
      __seoTitle: "Browser invechit"
    },
    no: {
      __content1: "Systemet har oppdaget at du bruker en utdatert nettleser. Det er anbefalt å bruke følgende nettlesere:",
      __content2: "Internet Explorer 10, Firefox 3.1, Safari 4, Chrome 4.1, Opera 30 eller nyere, eller andre nyere nettlesere for best opplevelse.",
      __seoTitle: "Utdatert nettleser"
    },
    sq: {
      __content1: "System detects it's an outdated browser. It is recommended using the following web browsers:",
      __content2: "IE10, Firefox31, Safari4, Chrome41, Opera30+ above version, or other up-to-date webkit browsers for the best possible experience.",
      __seoTitle: "Outdated Browser"
    },
    sl: {
      __content1: "Uporabljate nepodprt brskalnik. Priporočam, da uporabljate sledeče brskalnike:",
      __content2: "IE10, Firefox31, Safari4, Chrome41, Opera30+ ali višje verzije, za najboljšo uporabniško izkušnjo.",
      __seoTitle: "Zastarel brskalnik"
    },
    sr: {
      __content1: "System detects it's an outdated browser. It is recommended using the following web browsers:",
      __content2: "IE10, Firefox31, Safari4, Chrome41, Opera30+ above version, or other up-to-date webkit browsers for the best possible experience.",
      __seoTitle: "Outdated Browser"
    }
  }
}(window, document), function (e, t, i) {
  function o (e, t) {
    return typeof e === t
  }

  var r = [], n = [], s = {
    _version: "3.5.0",
    _config: {
      classPrefix: "",
      enableClasses: !0,
      enableJSClass: !0,
      usePrefixes: !0
    },
    _q: [],
    on: function (e, t) {
      var i = this;
      setTimeout(function () {
        t(i[e])
      }, 0)
    },
    addTest: function (e, t, i) {
      n.push({
        name: e,
        fn: t,
        options: i
      })
    },
    addAsyncTest: function (e) {
      n.push({
        name: null,
        fn: e
      })
    }
  }, a = function () {
  };
  a.prototype = s, a = new a;
  var l = t.documentElement, d = "svg" === l.nodeName.toLowerCase(),
  c = s._config.usePrefixes ? " -webkit- -moz- -o- -ms- ".split(" ") : ["", ""];
  s._prefixes = c, a.addTest("csscalc", function () {
    var e = function () {
      return "function" != typeof t.createElement ? t.createElement(arguments[0]) : d ? t.createElementNS.call(t, "http://www.w3.org/2000/svg", arguments[0]) : t.createElement.apply(t, arguments)
    }("a");
    return e.style.cssText = "width:" + c.join("calc(10px);width:"), !!e.style.length
  }), function () {
    var e, t, i, s, l, d;
    for(var c in n) if (n.hasOwnProperty(c)) {
      if (e = [], (t = n[c]).name && (e.push(t.name.toLowerCase()), t.options && t.options.aliases && t.options.aliases.length)) for(i = 0; i < t.options.aliases.length; i++) e.push(t.options.aliases[i].toLowerCase());
      for(s = o(t.fn, "function") ? t.fn() : t.fn, l = 0; l < e.length; l++) 1 === (d = e[l].split(".")).length ? a[d[0]] = s : (!a[d[0]] || a[d[0]] instanceof Boolean || (a[d[0]] = new Boolean(a[d[0]])), a[d[0]][d[1]] = s), r.push((s ? "" : "no-") + d.join("-"))
    }
  }(), function (e) {
    var t = l.className, i = a._config.classPrefix || "";
    if (d && (t = t.baseVal), a._config.enableJSClass) {
      var o = new RegExp("(^|\\s)" + i + "no-js(\\s|$)");
      t = t.replace(o, "$1" + i + "js$2")
    }
    a._config.enableClasses && (t += " " + i + e.join(" " + i), d ? l.className.baseVal = t : l.className = t)
  }(r), delete s.addTest, delete s.addAsyncTest;
  for(var u = 0; u < a._q.length; u++) a._q[u]();
  e._Modernizr = a
}(window, document), function (e, t) {
  function i () {
    this.ua = new e.UAParser, this.browserInfo = this.ua.getBrowser(), this.deviceInfo = this.ua.getDevice(), this.browserCheck = !0, e.YQ && e.YQ.configs && void 0 !== e.YQ.configs.browserCheck && (this.browserCheck = e.YQ.configs.browserCheck), this.init()
  }

  i.prototype = {
    init: function () {
      this.browserCheck && this.unSupport() && this.lowVersionBrowserHandle()
    },
    unSupport: function () {
      var e = this.isMobile(), t = this.isTablet(), i = this.isWebView();
      if (e || t) {
        if (this.browserCheck && i) return !1;
        if (!_Modernizr.csscalc) return !0
      } else if (this.lowBrowserIE(this.browserInfo) || this.lowBrowserRange(this.browserInfo, "Firefox", "31") || this.lowBrowserRange(this.browserInfo, "Chrome", "41") || this.lowBrowserRange(this.browserInfo, "Opera", "30") || this.lowBrowserRange(this.browserInfo, "Safari", "6")) return !0;
      return !1
    },
    isMobile: function () {
      return "mobile" === this.deviceInfo.type || "smarttv" === this.deviceInfo.type || "wearable" === this.deviceInfo.type || "embedded" === this.deviceInfo.type
    },
    isTablet: function () {
      return "tablet" === this.deviceInfo.type
    },
    isWebView: function () {
      var t = new RegExp("(^|&)from=([^&]*)(&|$)"), i = e.location.search.substr(1).match(t), o = !1;
      return null != i && "app" === unescape(i[2]) && (o = !0), o
    },
    lowBrowserIE: function (e) {
      return "IE5" === e.name || "IE6" === e.name || "IE7" === e.name || "IE8" === e.name || "IE9" === e.name || "IE" === e.name && e.major < 10
    },
    lowBrowserRange: function (e, t, i) {
      if (!_Modernizr.csscalc) return !0;
      t = t.replace(/\s/g, "").toLowerCase(), i = parseFloat(i);
      var o = e.name.replace(/\s/g, "").toLowerCase(), r = parseFloat(e.major);
      return o === t && r < i
    },
    updateHtmlTpl: function (e, t) {
      return '<style>body>*{display:none!important}</style><div class="yq-brower-update"><div class="yq-lowbrower-container"><div class="yq-tips-baby"><i></i></div><div class="yq-lbtips-content"><a href="javascript:void(0)" class="yq-lbtips-logo"></a><div class="yq-lbtips-text"><p>' + e + "</p><p>" + t + '</p></div><div class="yq-lbtips-download"><div class="icon"><i class="chrome"></i><span>Chrome</span></div><div class="icon"><i class="ie11"></i><span>IE11</span></div></div></div></div></div>'
    },
    lowVersionBrowserHandle: function () {
      var e = t.getElementsByTagName("html")[0].getAttribute("lang"), i = update_i18n[e] || update_i18n.en,
      o = i.__content1, r = i.__content2, n = i.__seoTitle, s = this.updateHtmlTpl(o, r);
      t.body.innerHTML = s, t.title = n + " | 17TRACK"
    }
  }, e.YQBrowserCheck = new i
}(window, document), window.YQ = window.YQ || {}, YQ.UI = {
  byId: function (e) {
    return e && e.tagName ? e : document.getElementById(e)
  },
  byClass: function (e, t) {
    if (t.getElementsByClass) return (t || document).getElementsByClass(e);
    for(var i = [], o = new RegExp("(^| )" + e + "( |$)"), r = this.byTagName("*", t), n = 0; n < r.length; n++) o.test(r[n].className) && i.push(r[n]);
    return i
  },
  byName: function (e) {
    return document.getElementsByName(e)
  },
  byTagName: function (e, t) {
    return (t || document).getElementsByTagName(e)
  },
  hasClass: function (e, t) {
    return e.className.match(new RegExp("(\\s|^)" + t + "(\\s|$)"))
  },
  addClass: function (e, t) {
    this.hasClass(e, t) || (e.className += " " + t)
  },
  removeClass: function (e, t) {
    if (this.hasClass(e, t)) {
      var i = new RegExp("(\\s|^)" + t + "(\\s|$)");
      e.className = e.className.replace(i, " ")
    }
  },
  offset: function (e) {
    for(var t = e.offsetLeft, i = e.offsetTop, o = e.offsetParent; null !== o;) t += o.offsetLeft, i += o.offsetTop, o = o.offsetParent;
    return {
      left: t,
      top: i
    }
  },
  winSize: function () {
    var e, t;
    return window.innerWidth ? (e = window.innerWidth, t = window.innerHeight) : document.body && document.body.clientWidth && (e = document.body.clientWidth, t = document.body.clientHeight), document.documentElement && document.documentElement.clientWidth && document.documentElement.clientHeight && (e = document.documentElement.clientWidth, t = document.documentElement.clientHeight), {
      width: e,
      height: t
    }
  },
  scrollLeft: function () {
    return document.documentElement.scrollLeft || document.body.scrollLeft
  },
  followPos: function (e, t) {
    var i = e, o = this.offset(i), r = this.scrollLeft(), n = o.left, s = n - r, a = (i.offsetWidth, t),
    l = this.winSize().width;
    return l - s - 20 >= a ? n : l - a - 20 + r
  },
  addEvent: function (e, t, i) {
    e.addEventListener ? e.addEventListener(t, i) : e.attachEvent("on" + t, function () {
      i.call(e)
    })
  }
}, YQ.FN = {
  getClientLang: function () {
    return navigator.browserLanguage || navigator.language || "en"
  },
  doTrackAppend: function (e) {
    var t = e.YQ_ContainerId, i = (YQ.UI.byId(t), e.YQ_Num.toString().replace(/(^\s*)|(\s*$)/g, "")), o = e.YQ_Fc || 0,
    r = e.YQ_Sc || 0, n = e.YQ_Lang || YQ.FN.getClientLang(), s = e.YQ_Height || YQ.ExtCall.maxHeight;
    if (!this.isSupport(n)) return !1;
    for(var a = "trackIframe-" + Math.floor(1e6 * Math.random()), l = {
      apitype: 1,
      uheight: s,
      nums: i,
      fc: o,
      sc: r,
      iframeId: a
    }, d = this.urlEncode(l), c = YQ.ExtCall.url + "/" + n + "/track#" + d, u = document.getElementById(t); u.firstChild;) u.removeChild(u.firstChild);
    if (YQ.UI.byId(t)) {
      var w = document.createElement("iframe");
      w.setAttribute("src", c), w.setAttribute("id", a), w.setAttribute("width", "100%"), w.setAttribute("height", 310), w.setAttribute("frameBorder", 0), w.style.border = "1px solid #e0e0e0", w.style.boxShadow = "0 0px 1px 0 rgba(0,0,0,.12)", w.style.minWidth = "260px", w.style.maxHeight = YQ.ExtCall.maxHeight + "px", YQ.UI.byId(t).style.display = "block", YQ.UI.byId(t).appendChild(w)
    }
  },
  doTrackBox: function (e) {
    var t, i, o, r, n, s, a, l, d;
    if (n = e.YQ_Lang || YQ.FN.getClientLang(), a = e.YQ_Num.toString().replace(/(^\s*)|(\s*$)/g, ""), s = e.YQ_Fc || 0, YQ_Sc = e.YQ_Sc || 0, o = e.YQ_Width || 322, r = e.YQ_Height || YQ.ExtCall.maxHeight, l = e.apiType, !this.isSupport(n)) return !1;
    var c = YQ.UI.winSize().width;
    o > c - 40 && (o = c - 40), t = YQ.UI.offset(e.follow).top + e.follow.offsetHeight + 10, i = YQ.UI.followPos(e.follow, o);
    var u = "trackIframe-" + Math.floor(1e6 * Math.random()), w = {
      apitype: l,
      uheight: r,
      nums: a,
      fc: s,
      sc: YQ_Sc,
      close: 1,
      iframeId: u
    }, m = this.urlEncode(w), p = YQ.ExtCall.url + "/" + n + "/track#" + m;
    (d = document.createElement("iframe")).setAttribute("id", u), d.setAttribute("width", o), d.setAttribute("height", 310), d.setAttribute("frameBorder", 0), d.setAttribute("class", "jcTrackIframe"), d.style.position = "absolute", d.style.left = i + "px", d.style.top = t + "px", d.style.zIndex = 99999, d.style.background = "#fff", d.style.border = "1px solid #e0e0e0", d.style.boxShadow = "0 0px 1px 0 rgba(0,0,0,.12)", d.style.minWidth = "260px", d.style.maxHeight = YQ.ExtCall.maxHeight + "px", d.setAttribute("src", p), document.body.appendChild(d)
  },
  isSupport: function (e) {
    if (YQBrowserCheck.unSupport()) {
      var t = e && update_i18n[e] || update_i18n.en;
      t.__content1, t.__content2;
      return alert(t.__content1 + "\n" + t.__content2), !1
    }
    return !0
  },
  urlEncode: function (e, t) {
    if ("[object Object]" !== Object.prototype.toString.call(e)) return "";
    var i = "";
    for(var o in e) i += "&" + o + "=" + (null == t || t ? encodeURIComponent(e[o]) : e[o]);
    return i.substr(1)
  }
}, function (e, t, i) {
  i.configs && i.configs.env;
  i.ExtCall = {
    url: "https://extcall.17track.net",
    minHeight: 488,
    maxHeight: 800
  }, window.YQV5 = {
    trackSingle: function (e) {
      i.FN.doTrackAppend(e)
    },
    trackMulti: function (e) {
      var t = i.UI.byId(e.YQ_ContainerId);
      if (t) {
        var o, r = e.YQ_Width || "100%", n = e.YQ_Height || i.ExtCall.minHeight;
        o = e.YQ_Lang ? i.ExtCall.url + "/" + e.YQ_Lang + "/multiline" : i.ExtCall.url + "/multiline";
        var s = document.createElement("iframe");
        s.setAttribute("src", o + "#apitype=2"), s.setAttribute("width", r), s.setAttribute("height", n), s.setAttribute("frameBorder", 0), s.style.minHeight = "356px", s.style.maxHeight = i.ExtCall.maxHeight + "px", t.appendChild(s)
      }
    },
    trackSingleF1: function (e) {
      var o = i.UI.byId(e.YQ_ElementId);
      o && i.UI.addEvent(o, "click", function () {
        var r = e.YQ_Width, n = e.YQ_Height, s = e.YQ_Lang || i.FN.getClientLang(), a = e.YQ_Num, l = e.YQ_Fc || 0,
        d = e.YQ_Sc || 0, c = "Iframe-" + e.YQ_ElementId;
        if (!i.UI.byId(c)) {
          var u = i.UI.byClass("jcTrackIframe", t)[0];
          u && u.parentNode.removeChild(u), i.FN.doTrackBox({
            YQ_Width: r,
            YQ_Height: n,
            YQ_Lang: s,
            YQ_Num: a,
            YQ_Fc: l,
            YQ_Sc: d,
            follow: o,
            apiType: 3
          })
        }
      })
    },
    trackSingleF2: function (e) {
      var o = i.UI.byId(e.YQ_ElementId);
      if (o) {
        var r = e.YQ_Width, n = e.YQ_Height, s = e.YQ_Lang || i.FN.getClientLang(), a = e.YQ_Num, l = e.YQ_Fc || 0,
        d = e.YQ_Sc || 0, c = (e.YQ_ElementId, i.UI.byClass("jcTrackIframe", t)[0]);
        c && c.parentNode.removeChild(c), i.FN.doTrackBox({
          YQ_Width: r,
          YQ_Height: n,
          YQ_Lang: s,
          YQ_Num: a,
          YQ_Fc: l,
          YQ_Sc: d,
          follow: o,
          apiType: 4
        })
      }
    }
  }, window.yqtrack_v4 = function (e) {
    window.YQV5.trackSingle({
      YQ_ContainerId: e.container.id,
      YQ_Height: e.height,
      YQ_Lang: e.lng,
      YQ_Width: e.width,
      YQ_Num: e.num,
      YQ_Fc: e.et || 0
    })
  }, e.addEventListener("message", function (e) {
    if (e.origin && e.origin.match(/^http[s]?:\/\/extcall.17track.net/i)) {
      var t;
      if (e.data.setHeight) {
        var i = e.data.setHeight.msg;
        (t = document.getElementById(e.data.setHeight.iframeId)).setAttribute("height", i)
      }
      e.data.closeIframe && (t = document.getElementById(e.data.closeIframe), document.body.removeChild(t))
    }
  }, !1)
}(window, document, YQ);