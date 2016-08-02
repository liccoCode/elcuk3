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
  ).on('click', '.btn:contains(Preview)', (e) ->
    false
  ).on('change', "#title, #bulletPoint1, #bulletPoint2, #bulletPoint3, #bulletPoint4, #bulletPoint5, #searchTerms1, #searchTerms2, #searchTerms3, #searchTerms4, #searchTerms5, #productDesc", (e) ->
    replaceInvalidCharacters(@, e)
  )

  $(document).ready ->
# 页面初始化时校验非法字符
    $('#title, #bulletPoint1, #bulletPoint2, #bulletPoint3, #bulletPoint4, #bulletPoint5, #searchTerms1, #searchTerms2, #searchTerms3, #searchTerms4, #searchTerms5, #productDesc').trigger('change')

  $("[name^='s.aps.keyFeturess'],[name^='s.aps.searchTermss'],[name='s.aps.productDesc']").blur()

  # 方便提供自动加载其他 Selling 的功能
  $('#sellingPreview').on('click', '#sid_preview', (e) ->
    noty({text: _.template($('#tsp-show-template').html())({tsp: $(@).data('tsp')})})
    false
  ).on('change', 'input', (e) ->
#  自动补全的 sid 的功能条
    $input = $(@)
    return false if @value.length < 10

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
    "AMAZON_UK_Computers": ["ComputerComponent", "ComputerDriveOrStorage", "NotebookComputer",
      "PersonalComputer", "Printer", "Scanner", "VideoProjector"],
    "AMAZON_UK_ConsumerElectronics": ["AVFurniture", "Battery", "CableOrAdapter", "CameraLenses",
      "CameraOtherAccessories", "CameraPowerSupply", "CarElectronics",
      "ConsumerElectronics", "DigitalPictureFrame", "GpsOrNavigationSystem", "Headphones",
      "Phone", "PhoneAccessory", "PhotographicStudioItems", "PortableAvDevice", "PowerSuppliesOrProtection",
      "RemoteControl", "Speakers", "Television", "camerabagsandcases"],
    "AMAZON_UK_Games": ["Software", "SoftwareGames", "VideoGames", "VideoGamesAccessories", "VideoGamesHardware"],
    "AMAZON_UK_HomeImprovement": ['BuildingMaterials', 'Electrical', 'Hardware', 'OrganizersAndStorage',
      'PlumbingFixtures', 'SecurityElectronics', 'Tools'],
    "AMAZON_UK_Kitchen": ["Kitchen"],

    "AMAZON_DE_Computers": ["ComputerComponent", "ComputerDriveOrStorage", "NotebookComputer",
      "PersonalComputer", "Printer", "Scanner", "VideoProjector"],
    "AMAZON_DE_ConsumerElectronics": ["AVFurniture", "Battery", "CableOrAdapter", "CameraLenses",
      "CameraOtherAccessories", "CameraPowerSupply", "CarElectronics",
      "ConsumerElectronics", "DigitalPictureFrame", "GpsOrNavigationSystem", "Headphones",
      "Phone", "PhoneAccessory", "PhotographicStudioItems", "PortableAvDevice", "PowerSuppliesOrProtection",
      "RemoteControl", "Speakers", "Television", "camerabagsandcases"],
    "AMAZON_DE_HomeImprovement": ['BuildingMaterials', 'Electrical', 'Hardware', 'OrganizersAndStorage',
      'PlumbingFixtures', 'SecurityElectronics', 'Tools'],
    "AMAZON_DE_Games": ["Software", "SoftwareGames", "VideoGames", "VideoGamesAccessories", "VideoGamesHardware"],
    "AMAZON_DE_Lighting": ["LightBulbs", "LightsAndFixtures"],
    "AMAZON_DE_Kitchen": ["Kitchen"],

    "AMAZON_US_Computers": ["ComputerComponent", "ComputerDriveOrStorage", "NotebookComputer",
      "PersonalComputer", "Printer", "Scanner", "VideoProjector"],
    "AMAZON_US_ConsumerElectronics": ["AVFurniture", "Battery", "CableOrAdapter", "CameraLenses",
      "CameraOtherAccessories", "CameraPowerSupply", "CarElectronics",
      "ConsumerElectronics", "DigitalPictureFrame", "GpsOrNavigationSystem", "Headphones",
      "Phone", "PhoneAccessory", "PhotographicStudioItems", "PortableAvDevice", "PowerSuppliesOrProtection",
      "RemoteControl", "Speakers", "Television", "camerabagsandcases"],
    "AMAZON_US_HomeImprovement": ['BuildingMaterials', 'Electrical', 'Hardware', 'MajorHomeAppliances',
      'OrganizersAndStorage', 'PlumbingFixtures', 'SecurityElectronics', 'Tools'],
    "AMAZON_US_Home": ['Art', 'BedAndBath', 'FurnitureAndDecor', 'Home', 'Kitchen', 'OutdoorLiving', 'SeedsAndPlants'],
    "AMAZON_US_Games": ["Software", "SoftwareGames", "VideoGames", "VideoGamesAccessories", "VideoGamesHardware"],
    "AMAZON_US_Sports": ["OutdoorRecreationProduct"],

    "AMAZON_FR_Computers": ["ComputerComponent", "ComputerDriveOrStorage", "NotebookComputer",
      "PersonalComputer", "Printer", "Scanner", "VideoProjector"],
    "AMAZON_FR_ConsumerElectronics": ["AVFurniture", "Battery", "CableOrAdapter", "CameraLenses",
      "CameraOtherAccessories", "CameraPowerSupply", "CarElectronics",
      "ConsumerElectronics", "DigitalPictureFrame", "GpsOrNavigationSystem", "Headphones",
      "Phone", "PhoneAccessory", "PhotographicStudioItems", "PortableAvDevice", "PowerSuppliesOrProtection",
      "RemoteControl", "Speakers", "Television", "camerabagsandcases"],

    "AMAZON_ES_Computers": ["ComputerComponent", "ComputerDriveOrStorage", "NotebookComputer",
      "PersonalComputer", "Printer", "Scanner", "VideoProjector"],
    "AMAZON_ES_ConsumerElectronics": ["AVFurniture", "Battery", "CableOrAdapter", "CameraLenses",
      "CameraOtherAccessories", "CameraPowerSupply", "CarElectronics",
      "ConsumerElectronics", "DigitalPictureFrame", "GpsOrNavigationSystem", "Headphones",
      "Phone", "PhoneAccessory", "PhotographicStudioItems", "PortableAvDevice", "PowerSuppliesOrProtection",
      "RemoteControl", "Speakers", "Television", "camerabagsandcases"],

    "AMAZON_IT_Computers": ["ComputerComponent", "ComputerDriveOrStorage", "NotebookComputer",
      "PersonalComputer", "Printer", "Scanner", "VideoProjector"],
    "AMAZON_IT_ConsumerElectronics": ["AVFurniture", "Battery", "CableOrAdapter", "CameraLenses",
      "CameraOtherAccessories", "CameraPowerSupply", "CarElectronics",
      "ConsumerElectronics", "DigitalPictureFrame", "GpsOrNavigationSystem", "Headphones",
      "Phone", "PhoneAccessory", "PhotographicStudioItems", "PortableAvDevice", "PowerSuppliesOrProtection",
      "RemoteControl", "Speakers", "Television", "camerabagsandcases"],
    "AMAZON_IT_Kitchen": ["Kitchen"],

    "AMAZON_JP_Computers": ["ComputerComponent", "ComputerDriveOrStorage", "NotebookComputer",
      "PersonalComputer", "Printer", "Scanner", "VideoProjector"],
    "AMAZON_JP_ConsumerElectronics": ["AVFurniture", "Battery", "CableOrAdapter", "CameraLenses",
      "CameraOtherAccessories", "CameraPowerSupply", "CarElectronics",
      "ConsumerElectronics", "DigitalPictureFrame", "GpsOrNavigationSystem", "Headphones",
      "Phone", "PhoneAccessory", "PhotographicStudioItems", "PortableAvDevice", "PowerSuppliesOrProtection",
      "RemoteControl", "Speakers", "Television", "camerabagsandcases"],

    "AMAZON_CA_Computers": ["ComputerComponent", "ComputerDriveOrStorage", "NotebookComputer",
      "PersonalComputer", "Printer", "Scanner", "VideoProjector"],
    "AMAZON_CA_ConsumerElectronics": ["AVFurniture", "Battery", "CableOrAdapter", "CameraLenses",
      "CameraOtherAccessories", "CameraPowerSupply", "CarElectronics",
      "ConsumerElectronics", "DigitalPictureFrame", "GpsOrNavigationSystem", "Headphones",
      "Phone", "PhoneAccessory", "PhotographicStudioItems", "PortableAvDevice", "PowerSuppliesOrProtection",
      "RemoteControl", "Speakers", "Television", "camerabagsandcases"]
  }

  # 模板类型与 Feed Product Type 的组合使用
  $(document).on('adjust', '#feedProductType', (r) ->
    $feedProductType = $("#feedProductType")
    $templateType = $('#templateType')
    $market = $('#market')
    return if _.isEmpty($market.val()) || _.isEmpty($templateType.val())

    feedTypes = feedProductTypeMap["#{$market.val()}_#{$templateType.val()}"]
    $feedProductType.empty()

    if _.isEmpty(feedTypes)
      noty({
        text: "暂不支持 #{$templateType.val()} 模板. 请联系开发人员添加新的模板.(备注: US 与 FR 市场可使用 Home 模板来替代 Kitchen 模板进行上架)",
        type: 'error',
        timeout: 5000
      })
      return

    _.each(feedTypes, (f) ->
      $feedProductType.append("<option value='#{f}'>#{f}</option>")
    )

    if templateType is "Games"
      $("#hardwarePlatformHome").show("slow")
    else
      $("#hardwarePlatformHome").hide("slow")
  )

  # 市场 下拉项变化 feedProductType 跟着变化
  $(document).on('change', '#market', (r) ->
    $("#feedProductType").trigger('adjust')
    # 市场变化时检查非法字符
    $('#title, #bulletPoint1, #bulletPoint2, #bulletPoint3, #bulletPoint4, #bulletPoint5, #searchTerms1, #searchTerms2, #searchTerms3, #searchTerms4, #searchTerms5, #productDesc').trigger('change')
    # 改变字符长度
    $("[name^='s.aps.keyFeturess'],[name^='s.aps.searchTermss'],[name='s.aps.productDesc']").blur()
  )

  # 模板 下拉项变化 feedProductType 跟着变化
  $(document).on('change', '#templateType', (r) ->
    $("#feedProductType").trigger('adjust')
  )

  # hints...
  $('#feedProductType').popover({
    trigger: 'focus',
    content: '修改这个值请非常注意, Amazon 对大类别下的产品的 Product Type 有严格的规定, 请参考 Amazon 文档进行处理'
  })
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
      $("#upc").val($("#upc_jp").val())
      $("#partNumber").val($("#partNumber_jp").val())
      $("#msku").val($("#msku").val().split(',')[0] + "," + $("#upc_jp").val())
    else
      Invalid_Characters = $.extend(EU_And_US_Invalid_Characters, JP_Invalid_Characters)
      $("#upc").val($("#upc_init").val())
      $("#partNumber").val($("#partNumber_init").val())
      $("#msku").val($("#msku").val().split(',')[0] + "," + $("#upc_init").val())
    for key, value of Invalid_Characters
      if str.indexOf(key) >= 0
        reg = "/#{key}/g"
        $(obj).val(str.replace(eval(reg), value))