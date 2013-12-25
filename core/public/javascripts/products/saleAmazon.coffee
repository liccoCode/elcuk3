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

  #初始化弹出提示框
  $('#RBN').popover({html: true, trigger: "focus", placement: "right", content: "具体值请下载后查阅该文件：<a href='https://images-na.ssl-images-amazon.com/images/G/01/rainier/help/btg/uk_computers_browse_tree_guide.xls' target='download'>下載</a>", title: "小提示^_^"}).popover('hide')

  # 账户下拉项变化RBN下载链接跟着改变
  $('#templateType').change ->
    #修改RBN下载地址
    updateRBNLink($('#market').val(), $('#templateType').val())
    uodateFeedProductType($('#market').val(), $('#templateType').val())


  # 更新RBN的提示信息
  updateRBNLink = (market, templateType) ->
    $.getJSON('/products/showRBNLink', {market: market, templateType: templateType})
    .done((r) ->
        $('#RBN').popover('destroy')
        $('#RBN').popover({html: true, trigger: "focus", placement: "right", content: "具体值请下载后查阅该文件：<a href='#{r.message}' target='download'>下載</a>", title: "小提示^_^"}).popover('hide')
      )

  # 更新FeedProductType信息
  uodateFeedProductType = (market, templateType) ->
    # 首先清空下拉项的数据
    jQuery("#feedProductType").empty()
    if templateType == "ConsumerElectronics"
      if market == "AMAZON_DE" or "AMAZON_UK"
        feedTypes = "AVFurniture_AccessoryOrPartOrSupply_AudioOrVideo_Battery_Binocular_CableOrAdapter_CameraFlash_CameraLenses_CameraOtherAccessories_CameraPowerSupply_CarElectronics_ConsumerElectronics_DigitalCamera_DigitalPictureFrame_FilmCamera_GpsOrNavigationSystem_Headphones_Phone_PhoneAccessory_PhotographicStudioItems_PortableAvDevice_PowerSuppliesOrProtection_Radio_RemoteControl_Speakers_Telescope_Television_VideoProjector_camerabagsandcases"
      if market == "AMAZON_US"
        feedTypes = "AVFurniture_Antenna_AudioVideoAccessory_BarCodeReader_Battery_BlankMedia_CableOrAdapter_CarAlarm_CarAudioOrTheater_CarElectronics_DVDPlayerOrRecorder_DigitalVideoRecorder_GPSOrNavigationAccessory_GPSOrNavigationSystem_HandheldOrPDA_Headphones_HomeTheaterSystemOrHTIB_MediaPlayer_MediaPlayerOrEReaderAccessory_MediaStorage_MiscAudioComponents_Phone_PortableAudio_PowerSuppliesOrProtection_RadarDetector_RadioOrClockRadio_ReceiverOrAmplifier_RemoteControl_Speakers_StereoShelfSystem_TVCombos_Television_Tuner_TwoWayRadio_VCR_VideoProjector"
      false
    if templateType == "Computers"
      if market == "AMAZON_DE" or "AMAZON_UK"
        feedTypes = "ComputerComponent_ComputerDriveOrStorage_Monitor_NotebookComputer_PersonalComputer_Printer_Scanner_VideoProjector"
      if market == "AMAZON_US"
        feedTypes = "CarryingCaseOrBag_Computer_ComputerAddOn_ComputerComponent_ComputerCoolingDevice_ComputerDriveOrStorage_ComputerInputDevice_ComputerProcessor_ComputerSpeaker_FlashMemory_Keyboards_MemoryReader_Monitor_Motherboard_NetworkingDevice_NotebookComputer_PersonalComputer_RAMMemory_SoundCard_SystemCabinet_SystemPowerDevice_TabletComputer_VideoCard_VideoProjector_Webcam"
      false
    arr = null
    arr = feedTypes.split("_")
    #循环数组，给selectd的option赋值
    for feedType in arr
      jQuery("#feedProductType").append("<option value='#{feedType}'>#{feedType}</option>")

  # 默认加载英国市场Computer模板的FeedProductType
  uodateFeedProductType("AMAZON_UK", "Computers")

