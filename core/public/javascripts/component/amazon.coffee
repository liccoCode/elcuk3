$ ->
  # 检查字符串长度
  validateMaxLength = (maxLength, obj) ->
    $text = $(obj)
    length = unescape(encodeURI(jsEscapeHtml($text.val().trim()))).length
    $text.find('~ .help-inline').html((maxLength - length) + " bytes left")
    if length > maxLength then $text.css('color', 'red') else $text.css('color', '')
    false

  jsEscapeHtml = (string) ->
    $("<div/>").text(string).html()

  # 预览 Desc 的方法
  previewBtn = (e) ->
    invalidTag = false
    for tag in $('#previewDesc').html($('#productDesc').val()).find('*')
      switch tag.nodeName.toString().toLowerCase()
        when 'br','p','b','#text'
          break
        else
          invalidTag = true
          $(tag).css('background', 'yellow')
    noty({text: '使用了 Amazon 不允许使用的 Tag, 请查看预览中黄色高亮部分!', type: 'error', timeout: 3000}) if invalidTag is true

  valid_length = (element) ->
    if element.getAttribute('id').indexOf('bulletPoint') > -1
      2000
    else if element.getAttribute('id').indexOf('searchTerms') > -1
      if $("#market").val() == 'AMAZON_FR'
        2000
      else
        50
    else if element.getAttribute('id').indexOf('productDesc') > -1
      2000
    else
      1000

  # bullet_point 的检查, search Terms 的检查, Product DESC 输入, 字数计算
  $('#saleAmazonForm').on('keyup blur', "[name^='s.aps.keyFeturess'],[name^='s.aps.searchTermss']", (e) ->
    return false if e.keyCode is 13
    validateMaxLength(valid_length(@), @)
  ).on('keyup', "[name='s.aps.productDesc']", (e) ->
    validateMaxLength(valid_length(@), @)
  ).on('blur', "[name='s.aps.productDesc']", (e) ->
    validateMaxLength(valid_length(@), @)
    previewBtn.call(@, e)
  ).on('click', '.btn:contains(Preview)', (e) ->
    previewBtn.call(@, e)
    false
  ).on('change', "#title, #bulletPoint1, #bulletPoint2, #bulletPoint3, #bulletPoint4, #bulletPoint5, #searchTerms1, #searchTerms2, #searchTerms3, #searchTerms4, #searchTerms5, #productDesc", (e) ->
    replaceInvalidCharacters(@, e)
  ).on('change', ".ke-content", (e) ->
    alert 1
  )

  $(document).ready ->
    # 页面初始化时校验非法字符
    $('#title, #bulletPoint1, #bulletPoint2, #bulletPoint3, #bulletPoint4, #bulletPoint5, #searchTerms1, #searchTerms2, #searchTerms3, #searchTerms4, #searchTerms5, #productDesc').trigger('change')

  $("[name^='s.aps.keyFeturess'],[name^='s.aps.searchTermss'],[name='s.aps.productDesc']").blur()

  # 方便提供自动加载其他 Selling 的功能
  $('#sellingPreview').on('click', '#sid_preview',(e) ->
    noty({text: _.template($('#tsp-show-template').html(), {tsp: $(@).data('tsp')})})
    false
  ).on('change', 'input',(e) ->
    #  自动补全的 sid 的功能条
    $input = $(@)
    return false if @value.length<10

    LoadMask.mask()
    $.ajax('/sellings/tsp', {type: 'GET', data: {sid: @value}, dataType: 'json'})
      .done((r) ->
        $('#sid_preview').data('tsp', r)
        noty({text: '加载成功, 可点击 "放大镜" 查看详细信息或者点击 "填充" 进行填充', type: 'success', timeout: 3000})
        LoadMask.unmask()
      )
  ).on('click', 'button:contains(填充)', (e) ->
    # 加载 tsp 数据的按钮
    json = $('#sid_preview').data('tsp')
    if json is undefined
      noty({text: '还没有数据, 请先预览!', type: 'warning', timeout: 3000})
      return false
    # product Desc
    $("[name='s.aps.productDesc']").val(json['p'][0]).blur()

    $("[name='s.aps.manufacturer']").val(json['man'][0]).blur()
    $("[name='s.aps.brand']").val(json['brand'][0]).blur()
    $("[name='s.aps.standerPrice']").val(json['price'][0]).blur()
    $("[name='s.aps.itemType']").val(json['type'][0]).blur()
    $("[name='s.aps.title']").val(json['title'][0]).blur()


    # technical
    tech = json['t']
    $("[name*='s.aps.keyFeturess']").each((i) ->
      $(@).val(if tech[i] then tech[i] else '').blur()
    )
    # searchTerms
    search = json['s']
    $("[name*='s.aps.searchTermss']").each((i) ->
      $(@).val(if search[i] then search[i] else '').blur()
    )
    false
  )

  # 定义 Feed Product Type 所有Map组合 Key为market_templatetype value为对应的feedProductType
  # 注：美国市场下载的模板文件名为Wireless 且该模板没有productType字段
  feedProductTypeMap = {
    "AMAZON_UK_Computers":["ComputerComponent", "ComputerDriveOrStorage", "Monitor", "NotebookComputer", "PersonalComputer", "Printer", "Scanner", "VideoProjector"],
    "AMAZON_UK_ConsumerElectronics":["AVFurniture", "AccessoryOrPartOrSupply", "AudioOrVideo", "Battery", "Binocular", "CableOrAdapter", "CameraFlash", "CameraLenses", "CameraOtherAccessories", "CameraPowerSupply", "CarElectronics", "ConsumerElectronics", "DigitalCamera", "DigitalPictureFrame", "FilmCamera", "GpsOrNavigationSystem", "Headphones", "Phone", "PhoneAccessory", "PhotographicStudioItems", "PortableAvDevice", "PowerSuppliesOrProtection", "Radio", "RemoteControl", "Speakers", "Telescope", "Television", "VideoProjector", "camerabagsandcases"],
    "AMAZON_UK_Games":["Software", "SoftwareGames", "VideoGames", "VideoGamesAccessories", "VideoGamesHardware"],
    "AMAZON_UK_HomeImprovement":['BuildingMaterials', 'Electrical', 'Hardware', 'OrganizersAndStorage', 'PlumbingFixtures', 'SecurityElectronics', 'Tools'],
    "AMAZON_UK_Sports":["SportingGoods"],

    "AMAZON_DE_Computers":["ComputerComponent", "ComputerDriveOrStorage", "Monitor", "NotebookComputer", "PersonalComputer", "Printer", "Scanner", "VideoProjector"],
    "AMAZON_DE_ConsumerElectronics":["AVFurniture", "AccessoryOrPartOrSupply", "AudioOrVideo", "Battery", "Binocular", "CableOrAdapter", "CameraFlash", "CameraLenses", "CameraOtherAccessories", "CameraPowerSupply", "CarElectronics", "ConsumerElectronics", "DigitalCamera", "DigitalPictureFrame", "FilmCamera", "GpsOrNavigationSystem", "Headphones", "Phone", "PhoneAccessory", "PhotographicStudioItems", "PortableAvDevice", "PowerSuppliesOrProtection", "Radio", "RemoteControl", "Speakers", "Telescope", "Television", "VideoProjector", "camerabagsandcases"],
    "AMAZON_DE_HomeImprovement":['BuildingMaterials', 'Electrical', 'Hardware',  'OrganizersAndStorage', 'PlumbingFixtures',  'SecurityElectronics', 'Tools'],
    "AMAZON_DE_Games":["Software", "SoftwareGames", "VideoGames", "VideoGamesAccessories", "VideoGamesHardware"],
    "AMAZON_DE_Sports":["SportingGoods"],
    "AMAZON_DE_Lighting":["LightBulbs", "LightsAndFixtures"],

    "AMAZON_US_Computers":["CarryingCaseOrBag", "Computer", "ComputerAddOn", "ComputerComponent", "ComputerCoolingDevice", "ComputerDriveOrStorage", "ComputerInputDevice", "ComputerProcessor", "ComputerSpeaker", "FlashMemory", "Keyboards", "MemoryReader", "Monitor", "Motherboard", "NetworkingDevice", "NotebookComputer", "PersonalComputer", "RAMMemory", "SoundCard", "SystemCabinet", "SystemPowerDevice", "TabletComputer", "VideoCard", "VideoProjector", "Webcam"],
    "AMAZON_US_ConsumerElectronics":["AVFurniture", "Antenna", "AudioVideoAccessory", "BarCodeReader", "Battery", "BlankMedia", "CableOrAdapter", "CarAlarm", "CarAudioOrTheater", "CarElectronics", "DVDPlayerOrRecorder", "DigitalVideoRecorder", "GPSOrNavigationAccessory", "GPSOrNavigationSystem", "HandheldOrPDA", "Headphones", "HomeTheaterSystemOrHTIB", "MediaPlayer", "MediaPlayerOrEReaderAccessory", "MediaStorage", "MiscAudioComponents", "Phone", "PortableAudio", "PowerSuppliesOrProtection", "RadarDetector", "RadioOrClockRadio", "ReceiverOrAmplifier", "RemoteControl", "Speakers", "StereoShelfSystem", "TVCombos", "Television", "Tuner", "TwoWayRadio", "VCR", "VideoProjector"],
    "AMAZON_US_HomeImprovement":['BuildingMaterials', 'Electrical', 'Hardware', 'MajorHomeAppliances', 'OrganizersAndStorage', 'PlumbingFixtures', 'SecurityElectronics', 'Tools'],
    "AMAZON_US_Wireless":["US市场下Wireless模板不包含该字段"],
    "AMAZON_US_Home":['Art', 'BedAndBath', 'FurnitureAndDecor', 'Home', 'Kitchen', 'OutdoorLiving', 'SeedsAndPlants'],
    "AMAZON_US_Games":["Software", "SoftwareGames", "VideoGames", "VideoGamesAccessories", "VideoGamesHardware"],
    "AMAZON_US_Sports":["OutdoorRecreationProduct"],

    "AMAZON_FR_Computers":['ComputerComponent', 'ComputerDriveOrStorage', 'Monitor', 'NotebookComputer', 'PersonalComputer', 'Printer', 'Scanner', 'VideoProjector'],
    "AMAZON_FR_ConsumerElectronics":['AVFurniture', 'AccessoryOrPartOrSupply', 'AudioOrVideo', 'Battery', 'Binocular', 'CableOrAdapter', 'Camcorder', 'CameraFlash', 'CameraLenses', 'CameraOtherAccessories', 'CameraPowerSupply', 'CarElectronics', 'ConsumerElectronics', 'DigitalCamera', 'DigitalPictureFrame', 'FilmCamera', 'GpsOrNavigationSystem', 'Headphones', 'Phone', 'PhoneAccessory', 'PhotographicStudioItems', 'PortableAvDevice', 'PowerSuppliesOrProtection', 'Radio', 'RemoteControl', 'Speakers', 'Telescope', 'Television', 'VideoProjector', 'camerabagsandcases'],

    "AMAZON_ES_Computers":['NotebookComputer', 'PersonalComputer', 'Monitor', 'VideoProjector', 'Printer', 'Scanner', 'InkOrToner', 'ComputerComponent', 'ComputerDriveOrStorage'],
    "AMAZON_ES_ConsumerElectronics":[ 'AVFurniture', 'AccessoryOrPartOrSupply', 'AudioOrVideo', 'Battery', 'Binocular', 'CableOrAdapter', 'Camcorder', 'CameraFlash', 'CameraLenses', 'CameraOtherAccessories', 'CameraPowerSupply', 'CarElectronics', 'ConsumerElectronics', 'DigitalCamera', 'DigitalPictureFrame', 'FilmCamera', 'GpsOrNavigationSystem', 'Headphones', 'Phone', 'PhoneAccessory', 'PhotographicStudioItems', 'PortableAvDevice', 'PowerSuppliesOrProtection', 'Radio', 'RemoteControl', 'Speakers', 'Telescope', 'Television', 'VideoProjector', 'camerabagsandcases'],

    "AMAZON_IT_Computers":[ 'NotebookComputer', 'PersonalComputer', 'Monitor', 'VideoProjector', 'Printer', 'Scanner', 'InkOrToner', 'ComputerComponent', 'ComputerDriveOrStorage'],
    "AMAZON_IT_ConsumerElectronics":[ 'AVFurniture', 'AccessoryOrPartOrSupply', 'AudioOrVideo', 'Battery', 'Binocular', 'CableOrAdapter', 'Camcorder', 'CameraFlash', 'CameraLenses', 'CameraOtherAccessories', 'CameraPowerSupply', 'CarElectronics', 'ConsumerElectronics', 'DigitalCamera', 'DigitalPictureFrame', 'FilmCamera', 'GpsOrNavigationSystem', 'Headphones', 'Phone', 'PhoneAccessory', 'PhotographicStudioItems', 'PortableAvDevice', 'PowerSuppliesOrProtection', 'Radio', 'RemoteControl', 'Speakers', 'Telescope', 'Television', 'VideoProjector', 'camerabagsandcases'],

    "AMAZON_JP_Computers":[ 'ComputerComponent', 'ComputerDriveOrStorage', 'InkOrToner', 'Monitor', 'NotebookComputer', 'PersonalComputer', 'Printer', 'Scanner', 'VideoProjector'],
    "AMAZON_JP_ConsumerElectronics":["AccessoryOrPartOrSupply", "AudioOrVideo", "AvFurniture", "Battery", "Binocular", "CableOrAdapter", "Camcorder", "CameraBagsAndCases", "DigitalCamera", "FilmCamera", "CameraFlash", "CameraLenses", "CameraOtherAccessories", "CameraPowerSupply", "CarElectronics", "ConsumerElectronics", "DigitalPictureFrame", "GpsOrNavigationSystem", "Headphones", "Phone", "PhoneAccessory", "PhotographicStudioItems", "PortableAvDevice", "PowerSuppliesOrProtection", "Radio", "RemoteControl", "Speakers", "Telescope", "Television", "VideoProjector"]

    "AMAZON_CA_Computers":["CarryingCaseOrBag", "Computer", "ComputerAddOn", "ComputerComponent", "ComputerCoolingDevice", "ComputerDriveOrStorage", "ComputerInputDevice", "ComputerProcessor", "ComputerSpeaker", "FlashMemory", "Keyboards", "MemoryReader", "Monitor", "Motherboard", "NetworkingDevice", "NotebookComputer", "PersonalComputer", "RAMMemory", "SoundCard", "SystemCabinet", "SystemPowerDevice", "TabletComputer", "VideoCard", "VideoProjector", "Webcam"],
    "AMAZON_CA_ConsumerElectronics":["AVFurniture", "Antenna", "AudioVideoAccessory", "Battery", "BlankMedia", "CableOrAdapter", "CarAlarm", "CarAudioOrTheater", "CarElectronics", "DVDPlayerOrRecorder", "DigitalVideoRecorder", "GPSOrNavigationAccessory", "GPSOrNavigationSystem", "HandheldOrPDA", "Headphones", "HomeTheaterSystemOrHTIB", "MediaPlayer", "MediaPlayerOrEReaderAccessory", "MediaStorage", "MiscAudioComponents", "PortableAudio", "PowerSuppliesOrProtection", "RadarDetector", "RadioOrClockRadio", "ReceiverOrAmplifier", "RemoteControl", "Speakers", "StereoShelfSystem", "TVCombos", "Television", "Tuner", "TwoWayRadio", "VCR", "VideoProjector"]
  }

  # 模板类型与 Feed Product Type 的组合使用
  $(document).on('adjust', '#feedProductType', (r) ->
    $feedProductType = $("#feedProductType")
    value = $feedProductType.val()
    $feedProductType.empty()
    templateType = $('#templateType').val()
    market = $('#market').val()
    fpkey = market + "_" + templateType

    feedTypes = feedProductTypeMap[fpkey]
    #循环数组，给selectd的option赋值
    _.each(feedTypes, (value) ->
      $feedProductType.append("<option value='#{value}'>#{value}</option>")
    )
    $feedProductType.val(value) if value
    #显示和隐藏hardwarePlatformHome
    if templateType is "Games" then $("#hardwarePlatformHome").show("slow")
    else $("#hardwarePlatformHome").hide("slow")
  )

  # 市场 下拉项变化 feedProductType 跟着变化
  $(document).on('change', '#market', (r) ->
    $("#feedProductType").trigger('adjust')
    # 市场变化时检查非法字符
    $('#title, #bulletPoint1, #bulletPoint2, #bulletPoint3, #bulletPoint4, #bulletPoint5, #searchTerms1, #searchTerms2, #searchTerms3, #searchTerms4, #searchTerms5, #productDesc').trigger('change')
  )

  # 模板 下拉项变化 feedProductType 跟着变化
  $(document).on('change', '#templateType', (r) ->
    $("#feedProductType").trigger('adjust')
  )

  # hints...
  $('#feedProductType').popover({trigger: 'focus', content: '修改这个值请非常注意, Amazon 对大类别下的产品的 Product Type 有严格的规定, 请参考 Amazon 文档进行处理'})
  $('#templateType').popover({trigger: 'focus', content: '为上传给 Amazon 的模板选择, 与 Amazon 的市场有关, 不可以随意修改'})
  $('#partNumber').popover({trigger: 'focus', content: '新 UPC 被使用后, Part Number 会被固定, 这个需要注意'})
  $('#state').popover({trigger: 'focus', content: 'NEW 状态 Selling 还没有同步回 ASIN, SELLING 状态为正常销售'})
  $('#itemType').popover({trigger: 'focus', content: '此属性字段为 UK 的 Games 模板独有，请填写 RBN 所对应的类别名称'})

  $sellingId = $("input[name='selling.sellingId']")
  $sellingId.typeahead({
    source: (query, process) ->
      sid = $sellingId.val()
      $.get('/sellings/sameSidSellings', {sid: sid})
      .done((c) ->
          process(c)
        )
  })

  EU_And_US_Invalid_Characters = {
    "，": ",",
    "。": ".",
    "！": "!",
    "（": "(",
    "）": ")",
    "——": "-",
    "—": "-",
    "、": ".",
    "；": ";",
    "‘": "\'",
    "’": "\'",
    "“": "\"",
    "”": "\"",
    "《": "<<",
    "》": ">>",
    "？": "?",
    "【": "[",
    "】": "]",
    "​": ""
  }

  JP_Invalid_Characters = {
    "——": "-",
    "—": "-",
    "✦": "",
    "•": "",
    "◉": "",
    "⦿": "",
    "▷": "",
    "▶": "",
    "❏": "",
    "❒": "",
    "♫": "",
    "®": "&reg;"
  }

  replaceInvalidCharacters = (obj, e) ->
    str = obj.value
    Invalid_Characters = {}
    market = $('#market').val()
    if market is 'AMAZON_JP'
      Invalid_Characters = JP_Invalid_Characters
    else
      Invalid_Characters = $.extend(EU_And_US_Invalid_Characters, JP_Invalid_Characters)
    for key, value of Invalid_Characters
      if str.indexOf(key) >= 0
        reg = "/#{key}/g"
        $(obj).val(str.replace(eval(reg), value))