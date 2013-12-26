$ ->
  $('#check_apply').click(->
    $('#msku').val(->
      $('#check_modal').modal('hide')
      "#{@value.split(",")[0]},#{$('#upc').val()}"
    )
    false
  )
  $('#check_cancel').click(->
    $('#msku').val(->
      $('#check_modal').modal('hide')
      "#{@value.split(",")[0]}"
    )
    false
  )

  # 显示 Selling 上架信息的 Modal 窗口
  showSellingModal = (title, sellings) ->
    modal = $('#check_modal').find('#upc_num').html(title).end()
    if sellings.length == 0
      modal.find('.innder-modal').html('<p>暂时没有上架 Selling</p>')
    else
      template = modal.find('.innder-modal').html('').end().find('.template')
      sellings.forEach (obj, index, arr) ->
        modal.find('.innder-modal').append(
          template.clone().removeClass('template').find('.check_id').html('SellingId: ' + obj.sellingId).end()
          .find('.check_title').html(obj.aps.title).end()
        )
    modal.modal('show')

  $('#market').change ->
    market = $(@).val()
    $.getJSON('/products/skuMarketCheck', {sku: $('#msku').val(), market: market})
    .done((r) ->
        if r.flag is false
          noty({text: r.message, type: 'warning'})
        else
          showSellingModal("#{$('#msku').val()} (#{r.length})", r)
      )
    false
    #市场下拉项变化 RBN下载地址跟着变化
    updateRBNLink(market, $('#templateType').val())
    uodateFeedProductType($('#market').val(), $('#templateType').val())

  # 账号对应的市场切换
  $('#account').change ->
    $("#market option:contains(#{$(@).find("option:selected").text().split('_')[0]})").prop('selected', true).change()

  # UPC 检查
  $('#upc').blur (e) ->
    $('#msku').val(->
      @value.split(',')[0])
    $self = $(@)
    upc = $(@).removeClass('btn-warning btn-success').val()
    if !$.isNumeric(upc)
      $self.addClass('btn-warning')
      noty({text: 'UPC 必须是数字', type: 'warning'})
      return false

    $.ajax('/products/upcCheck', {type: 'GET', data: {upc: upc}, dataType: 'json'})
      .done((r) ->
        if r.flag is false
          noty({text: r.message, type: 'error', layout: 'topCenter'})
        else
          showSellingModal("#{$('#msku').val()} (#{r.length})", r)
      )
    false

  $('#submitSale').click ->
    $form = $('#saleAmazonForm')
    LoadMask.mask()
    $.ajax($form.attr('action'), {data: $form.serialize(), method: 'POST'})
    .done((r) ->
        if r.flag
          noty({text: "成功创建 Selling #{r.message}"})
        else
          noty({text: r.message, type: 'error'})
        LoadMask.unmask()
      )
    .fail((r) ->
        noty({text: r.responseText, type: 'error'})
        LoadMask.unmask()

      )

  # 账户下拉项变化 RBN 下载链接跟着改变
  $('#templateType').change ->
    #修改 RBN 下载地址
    updateRBNLink($('#market').val(), $('#templateType').val())
    uodateFeedProductType($('#market').val(), $('#templateType').val())


  # 更新 RBN 的提示信息
  updateRBNLink = (market, templateType) ->
    $.getJSON('/products/showRBNLink', {market: market, templateType: templateType})
    .done((r) ->
        $('#RBN').popover('destroy')
        $('#RBN').popover({html: true, trigger: "focus", placement: "right", content: "具体值请下载后查阅该文件：<a href='#{r.message}' target='download'>下载</a>", title: "小提示^_^"}).popover('hide')
      )

  # 更新 FeedProductType 信息
  uodateFeedProductType = (market, templateType) ->
    # 首先清空下拉项的数据
    $feedProductType = $("#feedProductType").empty()

    feedTypes = if templateType == "ConsumerElectronics"
      if market == "AMAZON_DE" or "AMAZON_UK"
        ["AVFurniture", "AccessoryOrPartOrSupply", "AudioOrVideo", "Battery", "Binocular", "CableOrAdapter", "CameraFlash", "CameraLenses", "CameraOtherAccessories", "CameraPowerSupply", "CarElectronics", "ConsumerElectronics", "DigitalCamera", "DigitalPictureFrame", "FilmCamera", "GpsOrNavigationSystem", "Headphones", "Phone", "PhoneAccessory", "PhotographicStudioItems", "PortableAvDevice", "PowerSuppliesOrProtection", "Radio", "RemoteControl", "Speakers", "Telescope", "Television", "VideoProjector", "camerabagsandcases"]
      else if market ==  "AMAZON_US"
        ["AVFurniture", "Antenna", "AudioVideoAccessory", "BarCodeReader", "Battery", "BlankMedia", "CableOrAdapter", "CarAlarm", "CarAudioOrTheater", "CarElectronics", "DVDPlayerOrRecorder", "DigitalVideoRecorder", "GPSOrNavigationAccessory", "GPSOrNavigationSystem", "HandheldOrPDA", "Headphones", "HomeTheaterSystemOrHTIB", "MediaPlayer", "MediaPlayerOrEReaderAccessory", "MediaStorage", "MiscAudioComponents", "Phone", "PortableAudio", "PowerSuppliesOrProtection", "RadarDetector", "RadioOrClockRadio", "ReceiverOrAmplifier", "RemoteControl", "Speakers", "StereoShelfSystem", "TVCombos", "Television", "Tuner", "TwoWayRadio", "VCR", "VideoProjector"]
      else
        []
    else if templateType == "Computers"
      if market == "AMAZON_DE" or "AMAZON_UK"
        ["ComputerComponent", "ComputerDriveOrStorage", "Monitor", "NotebookComputer", "PersonalComputer", "Printer", "Scanner", "VideoProjector"]
      else if market == "AMAZON_US"
        ["CarryingCaseOrBag", "Computer", "ComputerAddOn", "ComputerComponent", "ComputerCoolingDevice", "ComputerDriveOrStorage", "ComputerInputDevice", "ComputerProcessor", "ComputerSpeaker", "FlashMemory", "Keyboards", "MemoryReader", "Monitor", "Motherboard", "NetworkingDevice", "NotebookComputer", "PersonalComputer", "RAMMemory", "SoundCard", "SystemCabinet", "SystemPowerDevice", "TabletComputer", "VideoCard", "VideoProjector", "Webcam"]
      else
        []

    #循环数组，给selectd的option赋值
    _.each(feedTypes, (value) ->
      $feedProductType.append("<option value='#{value}'>#{value}</option>")
    )

  # 默认加载 UK 英国市场 Computer 模板的 FeedProductType
  uodateFeedProductType("AMAZON_UK", "Computers")

  # hints
  $('#RBN').popover({html: true, trigger: "focus", placement: "right", content: "具体值请下载后查阅该文件：<a href='https://images-na.ssl-images-amazon.com/images/G/01/rainier/help/btg/uk_computers_browse_tree_guide.xls' target='download'>下载</a>", title: "小提示^_^"}).popover('hide')
