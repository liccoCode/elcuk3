$(() => {
  $("td[name='clickTd']").click(function () {
    let tr = $(this).parent("tr");
    let id = $(this).data("id");
    if ($("#div" + id).html() != undefined) {
      tr.next("tr").toggle();
    } else {
      let html = "<tr style='background-color:#F2F2F2'><td colspan='9'>";
      html += "<div id='div" + id + "'></div></td></tr>";
      tr.after(html);
      $("#div" + id).load("/BtbCustoms/showBtbOrderList", {id: id});
    }
  });

  $("#add_address_btn").click(function () {
    let index = $("input[name$='receiver']").length;
    let html = _.template($("#copy").text())({"num": index});
    $("#btn_tr").before(html);
  });

});
