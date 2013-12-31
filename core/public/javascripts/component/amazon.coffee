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
  )

  $("[name^='s.aps.keyFeturess'],[name^='s.aps.searchTermss'],[name='s.aps.productDesc']").blur()

  # 方便提供自动加载其他 Selling 的功能
  $('#sellingPreview').on('click', '#sid_preview',(e) ->
    noty({text: _.template($('#tsp-show-template').html(), {tsp: $(@).data('tsp')})})
    false
  ).on('change', 'input',(e) ->
    #  自动补全的 sid 的功能条
    $input = $(@)
    if $input.data('sids') is undefined
      $input.data('sids', $input.data('source'))
    return false if !(@value in $input.data('sids'))

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
    # technical
    tech = json['t']
    $("[name='s.aps.keyFeturess']").each((i) ->
      $(@).val(if tech[i] then tech[i] else '').blur()
    )
    # searchTerms
    search = json['s']
    $("[name='s.aps.searchTermss']").each((i) ->
      $(@).val(if search[i] then search[i] else '').blur()
    )
    false
  )

  # 定义 Feed Product Type 所有Map组合 Key为market_templatetype value为对应的feedProductType
  # 注：美国市场下载的模板文件名为Wireless 且该模板没有productType字段
  feedProductTypeMap = {"AMAZON_UK_Computers":["ComputerComponent", "ComputerDriveOrStorage", "Monitor", "NotebookComputer", "PersonalComputer", "Printer", "Scanner", "VideoProjector"],"AMAZON_UK_ConsumerElectronics":["AVFurniture", "AccessoryOrPartOrSupply", "AudioOrVideo", "Battery", "Binocular", "CableOrAdapter", "CameraFlash", "CameraLenses", "CameraOtherAccessories", "CameraPowerSupply", "CarElectronics", "ConsumerElectronics", "DigitalCamera", "DigitalPictureFrame", "FilmCamera", "GpsOrNavigationSystem", "Headphones", "Phone", "PhoneAccessory", "PhotographicStudioItems", "PortableAvDevice", "PowerSuppliesOrProtection", "Radio", "RemoteControl", "Speakers", "Telescope", "Television", "VideoProjector", "camerabagsandcases"],"AMAZON_DE_Computers":["ComputerComponent", "ComputerDriveOrStorage", "Monitor", "NotebookComputer", "PersonalComputer", "Printer", "Scanner", "VideoProjector"],"AMAZON_DE_ConsumerElectronics":["AVFurniture", "AccessoryOrPartOrSupply", "AudioOrVideo", "Battery", "Binocular", "CableOrAdapter", "CameraFlash", "CameraLenses", "CameraOtherAccessories", "CameraPowerSupply", "CarElectronics", "ConsumerElectronics", "DigitalCamera", "DigitalPictureFrame", "FilmCamera", "GpsOrNavigationSystem", "Headphones", "Phone", "PhoneAccessory", "PhotographicStudioItems", "PortableAvDevice", "PowerSuppliesOrProtection", "Radio", "RemoteControl", "Speakers", "Telescope", "Television", "VideoProjector", "camerabagsandcases"],"AMAZON_DE_HomeImprovement":['BuildingMaterials', 'Electrical', 'Hardware',  'OrganizersAndStorage', 'PlumbingFixtures',  'SecurityElectronics', 'Tools'],"AMAZON_US_Computers":["CarryingCaseOrBag", "Computer", "ComputerAddOn", "ComputerComponent", "ComputerCoolingDevice", "ComputerDriveOrStorage", "ComputerInputDevice", "ComputerProcessor", "ComputerSpeaker", "FlashMemory", "Keyboards", "MemoryReader", "Monitor", "Motherboard", "NetworkingDevice", "NotebookComputer", "PersonalComputer", "RAMMemory", "SoundCard", "SystemCabinet", "SystemPowerDevice", "TabletComputer", "VideoCard", "VideoProjector", "Webcam"],"AMAZON_US_ConsumerElectronics":["AVFurniture", "Antenna", "AudioVideoAccessory", "BarCodeReader", "Battery", "BlankMedia", "CableOrAdapter", "CarAlarm", "CarAudioOrTheater", "CarElectronics", "DVDPlayerOrRecorder", "DigitalVideoRecorder", "GPSOrNavigationAccessory", "GPSOrNavigationSystem", "HandheldOrPDA", "Headphones", "HomeTheaterSystemOrHTIB", "MediaPlayer", "MediaPlayerOrEReaderAccessory", "MediaStorage", "MiscAudioComponents", "Phone", "PortableAudio", "PowerSuppliesOrProtection", "RadarDetector", "RadioOrClockRadio", "ReceiverOrAmplifier", "RemoteControl", "Speakers", "StereoShelfSystem", "TVCombos", "Television", "Tuner", "TwoWayRadio", "VCR", "VideoProjector"],"AMAZON_US_HomeImprovement":['BuildingMaterials', 'Electrical', 'Hardware', 'MajorHomeAppliances', 'OrganizersAndStorage', 'PlumbingFixtures', 'SecurityElectronics', 'Tools'],"AMAZON_US_Wireless":["US市场Wireless模板不需要该字段"],"AMAZON_US_Home":['Art', 'BedAndBath', 'FurnitureAndDecor', 'Home', 'Kitchen', 'OutdoorLiving', 'SeedsAndPlants'],"AMAZON_FR_Computers":['ComputerComponent', 'ComputerDriveOrStorage', 'Monitor', 'NotebookComputer', 'PersonalComputer', 'Printer', 'Scanner', 'VideoProjector'],"AMAZON_FR_ConsumerElectronics":['AVFurniture', 'AccessoryOrPartOrSupply', 'AudioOrVideo', 'Battery', 'Binocular', 'CableOrAdapter', 'Camcorder', 'CameraFlash', 'CameraLenses', 'CameraOtherAccessories', 'CameraPowerSupply', 'CarElectronics', 'ConsumerElectronics', 'DigitalCamera', 'DigitalPictureFrame', 'FilmCamera', 'GpsOrNavigationSystem', 'Headphones', 'Phone', 'PhoneAccessory', 'PhotographicStudioItems', 'PortableAvDevice', 'PowerSuppliesOrProtection', 'Radio', 'RemoteControl', 'Speakers', 'Telescope', 'Television', 'VideoProjector', 'camerabagsandcases'],"AMAZON_ES_Computers":['NotebookComputer', 'PersonalComputer', 'Monitor', 'VideoProjector', 'Printer', 'Scanner', 'InkOrToner', 'ComputerComponent', 'ComputerDriveOrStorage'],"AMAZON_ES_ConsumerElectronics":[ 'AVFurniture', 'AccessoryOrPartOrSupply', 'AudioOrVideo', 'Battery', 'Binocular', 'CableOrAdapter', 'Camcorder', 'CameraFlash', 'CameraLenses', 'CameraOtherAccessories', 'CameraPowerSupply', 'CarElectronics', 'ConsumerElectronics', 'DigitalCamera', 'DigitalPictureFrame', 'FilmCamera', 'GpsOrNavigationSystem', 'Headphones', 'Phone', 'PhoneAccessory', 'PhotographicStudioItems', 'PortableAvDevice', 'PowerSuppliesOrProtection', 'Radio', 'RemoteControl', 'Speakers', 'Telescope', 'Television', 'VideoProjector', 'camerabagsandcases'],"AMAZON_IT_Computers":[ 'NotebookComputer', 'PersonalComputer', 'Monitor', 'VideoProjector', 'Printer', 'Scanner', 'InkOrToner', 'ComputerComponent', 'ComputerDriveOrStorage'],"AMAZON_IT_ConsumerElectronics":[ 'AVFurniture', 'AccessoryOrPartOrSupply', 'AudioOrVideo', 'Battery', 'Binocular', 'CableOrAdapter', 'Camcorder', 'CameraFlash', 'CameraLenses', 'CameraOtherAccessories', 'CameraPowerSupply', 'CarElectronics', 'ConsumerElectronics', 'DigitalCamera', 'DigitalPictureFrame', 'FilmCamera', 'GpsOrNavigationSystem', 'Headphones', 'Phone', 'PhoneAccessory', 'PhotographicStudioItems', 'PortableAvDevice', 'PowerSuppliesOrProtection', 'Radio', 'RemoteControl', 'Speakers', 'Telescope', 'Television', 'VideoProjector', 'camerabagsandcases']
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
  )

  # 市场 下拉项变化 feedProductType 跟着变化
  $(document).on('change', '#market', (r) ->
    $("#feedProductType").trigger('adjust')
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
