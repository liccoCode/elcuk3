$(() => {

  $("#categories").multiselect({
    buttonWidth: '120px',
    nonSelectedText: '品线',
    maxHeight: 200,
    includeSelectAllOption: true
  });

  function bindTrEvent () {
    $("td[name='clickTd']").click(function () {
      LoadMask.mask();
      let tr = $(this).parent("tr");
      let family = $(this).data("family");
      if ($("#div" + family).html() != undefined) {
        tr.next("tr").toggle();
        LoadMask.unmask();
      } else {
        let html = "<tr style='background-color:#F2F2F2'><td colspan='11'>" + family + "下产品<hr>";
        html += "<div id='div" + family + "'></div></td></tr>";
        tr.after(html);
        $("#div" + family).load($(this).data("url"), {name: family}, function () {
          LoadMask.unmask();
        });
      }
    });
  }

  $("button[name='add_family']").click(function (e) {
    e.preventDefault();
    $("#add_category_input").val($(this).data("category"));
    $("#add_brand_input").val($(this).data("brand"));
    $("#add_family_input").val($(this).data("category") + $(this).data("brand"));
    $("#add_modal").modal("show");
  });

  $("#add_family_input").on("keyup", function () {
    $(this).val($(this).val().toUpperCase());
  });

  $("input[name='brandRadio']").click(function () {
    if ($(this).prop("checked")) {
      $("#familyDiv").load($("#familyDiv").data("url"), {
        brand: $(this).val(),
        categoryId: $(this).data("category")
      }, function () {
        bindTrEvent();
      });
    }

  });

});



