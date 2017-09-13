$(() => {

  $("#addBom").click(function (e) {
    $("#bom_modal").modal('show')
  });

  $("#submitCreateBtn").click(function () {
    $("#create_form").submit();
  });



  $("td[name='clickTd']").click(function () {
    let tr = $(this).parent("tr");
    let id = $(this).data("id");
    let format_id = id;
    let type = $(this).data("type");
    if ($("#div" + format_id).html() != undefined) {
      tr.next("tr").toggle();
    } else {
      let html = "<tr style='background-color:#F2F2F2'><td colspan='13'><div id='div" + format_id + "'></div></td></tr>";
      tr.after(html);
      $("#data-table").mask();
      $("#div" + format_id).load($(this).data("url"), {id: id}, function () {
        $("#data-table").unmask();
      });
    }
  });

  //单个数据下架js处理
  $("#data-table a[name='delBtn']").click(function (e) {
    e.preventDefault();
    if (confirm("确定下架数据吗")) {
      $("#mid").val($(this).attr('uid'));
      return $('#search_Form').attr('action', $(this).data('url')).submit();
    }
  });

});