window.dropUpload =
# 图片的 Drag&Drop DIV 初始化
  template: '<li class="col-md-1">' +
    '<a href="#" target="_blank" class="thumbnail"><img/></a>' +
    '<div class="progress"><div class="bar"></div></div>' +
    '<div class="title" style="position:relative;word-break:break-all;font-weight:bold"></div>' +
    '<div class="title2" style="position:relative;word-break:break-all;" ></div>' +
    '<div class="title3" style="position:relative;word-break:break-all;" ></div>' +
    '<div class="action" style="position:relative;word-break:break-all;"><a href="#" style="color:red;"><i class="icon-remove"></i></a></div>' +
    '</li>'

  xlsImg: '/images/uploads/xls.jpg'
  docImg: '/images/uploads/doc.jpg'
  pptImg: '/images/uploads/ppt.jpg'
  pdfImg: '/images/uploads/pdf.jpg'
  zipImg: '/images/uploads/zip.jpg'

  # 删除服务器端的 Image, 同时删除页面中的 Image 元素
  rmImage: (e) ->
    return false if !confirm('确定要删除此附件?')
    o = $(this)
    $.post('/attachs/rm', {'a.outName': o.attr('outName')},
      (r) ->
        if(r.flag)
          alert("删除成功.")
        else
          alert(r.message)
        $('a[outName=' + o.attr('outName') + ']').parents('li').remove()
        return false
    )
    false

  imgSrc: (fileName, img, imgUrl) ->
    args = fileName.split('.')
    fileSuffix = args[args.length - 1].toLowerCase()
    switch fileSuffix
      when 'xls', 'xlsx', 'csv'
        img.attr('src', window.dropUpload.xlsImg)
      when 'doc', 'docx'
        img.attr('src', window.dropUpload.docImg)
      when 'ppt', 'pptx'
        img.attr('src', window.dropUpload.pptImg)
      when 'pdf'
        img.attr('src', window.dropUpload.pdfImg)
      when 'zip', 'rar', '7z'
        img.attr('src', window.dropUpload.zipImg)
      when 'jpg', 'jpeg', 'png', 'gif', 'bmp'
        img.attr('src', imgUrl)
      else
        img.attr('src', window.dropUpload.docImg)

  # 利用 Html 的 File API(FileReader) 创建图片的缩略图
  createImage: (file, uploaded) ->
    preview = $(window.dropUpload.template)
    img = $('img', preview)
    reader = new FileReader()
    reader.onload = (e) ->
      window.dropUpload.imgSrc(file.name, img, e.target.result)
      img.attr('title', file.name)
    # 直接将数据读成二进制字符串以便放在 URL 上显示,结果在 FileReader 的 result 中
    reader.readAsDataURL(file)
    preview.appendTo(uploaded)
    $.data(file, preview)

  #
  # dropbox, message, uploaded 参考下面的 HTML 代码
  #
  #<div style="min-height:300px;background:#eee;" id="dropbox">
  #    <ul class="thumbnails uploaded"></ul>
  #    <div class="message" style="height:150px;padding-top:145px;text-align:center;">Drag & Drop</div>
  #</div>
  # ----
  # fidFunc: 返回 a.fid(fid) 与 a.p(p) 的回掉方法(详情查看 javascripts/component/dropupload.coffee);
  iniDropbox: (fidAndAttachPFunc, dropbox, url = '/attachs/upload', fileparam = 'a.file') ->
    message = dropbox.find('.message')
    uploaded = dropbox.find('.uploaded')
    dropbox.filedrop(
      paramname: fileparam
      maxfiles: 20
      # in mb
      maxfilesize: 10
      url: url
      beforeEach: (file) ->
        # file is a file object
        # return false to cancel upload
        # 'a.fid':'90-kd'
        args = fidAndAttachPFunc()
        this.data['a.fid'] = args['fid']
        this.data['a.p'] = args['p']
        # 这里判断是在哪个 DIV 上传的文件，设定相对应的 attachType 参数值
        if dropbox.attr("id") == "dropbox"
          this.data['a.attachType'] = "IMAGE"
        if dropbox.attr("id") == "packageDropbox"
          this.data['a.attachType'] = "PACKAGE"
        if dropbox.attr("id") == "instructionsDropbox"
          this.data['a.attachType'] = "INSTRUCTION"
        if dropbox.attr("id") == "silkscreenDropbox"
          this.data['a.attachType'] = "SILKSCREEN"
        return false if this.data['a.fid'] is false
      uploadStarted: (i, file, len) ->
        #???
        window.dropUpload.createImage(file, uploaded)
      dragOver: ->
        dropbox.css('background', '#eff')
      dragLeave: ->
        dropbox.css('background', '#eee')
      drop: ->
        dropbox.css('background', '#eee')
      progressUpdated: (i, file, progress) ->
        $.data(file).find('.bar').width(progress + '%')
      uploadFinished: (i, file, r, time) ->
        $.data(file).find('.progress').remove()
        # ???
        message.remove() if message
        if r['flag'] is false
          alert(r.message)
        else
          $.data(file).find('a.thumbnail').attr("href", "/attachs/image?a.outName=" + r['outName'])
          $.data(file).find('div.action a').attr('outName', r['outName']).click(window.dropUpload.rmImage)
      error: (err, file) ->
        switch err
          when 'BrowserNotSupported'
            alert('browser does not support html5 drag and drop')
          when 'TooManyFile'
          # user uploaded more than 'maxfiles'
          # program encountered a file whose size is greater than 'maxfilesize'
            alert('TooManyFiles...')
          when 'FileTooLarge'
          # FileTooLarge also has access to the file which was too large
          # use file.name to reference the filename of the culprit file
            alert("File is too Large!")
    )

  # 初始化页面的时候加载此 Product 对应的附件; 根据 attachType 展示到对应的 div 内
  loadAttachs: (fid, p = '', cls = 'span2') ->
    # 图片 DIV
    dropbox = $('#dropbox')
    $.getJSON('/attachs/images', {
      fid: fid,
      p: p
    },
      (imgs) ->
        versionConut = 0
        for img, i in imgs
          type = img.attachType
          # 图片 DIV
          if type == "IMAGE"
            dropbox = $('#dropbox')

          # 包装 DIV
          else if type == "PACKAGE"
            dropbox = $('#packageDropbox')

          # 说明书 DIV
          else if type == "INSTRUCTION"
            dropbox = $('#instructionsDropbox')

          # 丝印文件 DIV
          else if type == "SILKSCREEN"
            dropbox = $('#silkscreenDropbox')

          uploaded = dropbox.find('.uploaded')
          message = dropbox.find('.message')
          message.remove() if(imgs.length > 0)
          attachEl = $(window.dropUpload.template)
          attachEl.addClass(cls)
          imgUrl = "/attachs/image?a.fileName=" + img['fileName']
          window.dropUpload.imgSrc(img['fileName'], attachEl.find("img"), imgUrl + "&w=140&h=100")
          attachEl.find('a.thumbnail').attr("href", imgUrl).attr('title', img['fileName'])
          attachEl.find('a[style]').attr('outName', img['outName']).click(window.dropUpload.rmImage)
          attachEl.find('div.progress').remove()
          if i - 1 >= 0
            # 如果文件名和上一个相同并且文件类型也相同，版本号 +1 （查询出来时已经按照文件名分好组，文件名相同的在一块）
            if(img['originName'] == imgs[i - 1]['originName'] && type == imgs[i - 1].attachType)
              ++versionConut
            else
              versionConut = 0
          attachEl.find('div.title').text("Version#{versionConut + 1}")
          attachEl.find('div.title2').text("文件名: " + img['originName'])
          attachEl.find('div.title3').text('创建日期: ' + img['createDate'])
          attachEl.appendTo(uploaded)
    )

  # 初始化页面的时候加载此 Product 对应的图片; dropbox 图片展示的 div
  loadImages: (fid, dropbox, p = '', cls = 'span2', removable = true) ->
    uploaded = dropbox.find('.uploaded')
    message = dropbox.find('.message')
    $.getJSON('/attachs/images', {
      fid: fid,
      p: p
    },
      (imgs) ->
        message.remove() if(imgs.length > 0)
        for img, i in imgs
          imgEl = $(window.dropUpload.template)
          imgEl.addClass(cls)
          #imgUrl = "/attachs/image?a.fileName=" + img['fileName']
          imgUrl =  img['qiniuLocation']
          window.dropUpload.imgSrc(img['fileName'], imgEl.find("img"), imgUrl)
          imgEl.find('a.thumbnail').attr("href", imgUrl).attr('title', img['fileName'])
          imgEl.find('a[style]').attr('outName', img['outName']).click(window.dropUpload.rmImage) if removable
          imgEl.find('div.progress').remove()
          imgEl.find('div.title').text(img['originName'])
          imgEl.appendTo(uploaded)
    )