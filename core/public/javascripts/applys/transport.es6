$(() => {

  $("#shipments").on("click", ".delete", function (e) {
    e.stopPropagation();
    let id = $(this).data('id');
    $.post("/apply/transport/" + id + "/shipment", function (r) {
      if (r.flag) {
        noty({
          text: "#" + id + r.message,
          type: 'success',
          timeout: 3000
        });
        $("#shipment_" + id.replace(/[|]/g, '\\|')).next().remove().end().remove();
      } else {
        noty({
          text: r.message,
          type: 'error'
        })
      }
    });
  });

  $("input[name='p.search']").typeahead({
    source: (query, process) => {
      $.get('/applys/source', {
        applyId: $("input[name='p.applyId']").val(),
        search: query
      }, function (c) {
        process(c)
      });
    }
  });

  let paymentUnitId = window.location.hash.slice(1);
  let targetTr = $("#fee_" + paymentUnitId);
  if (targetTr.size() > 0) {
    targetTr.parents('tr').prev().find('td[data-toggle]').click();
    EF.scoll(targetTr);
    EF.colorAnimate(targetTr);
  }

  $("#batch_approve_btn").click(function (e) {
    if ($("input[name='pids']:checked").length === 0) {
      noty({
        text: "请选择批准的费用记录",
        type: "error"
      });
    } else {
      let pids = [];
      $("input[name='pids']:checked").each(function () {
        pids.push($(this).val());
      });
      $.post($(this).data("url"), {pids: pids}, function (r) {
        if (r.flag) {
          noty({
            text: r.message,
            type: 'success',
            timeout: 3000
          });
        } else {
          noty({
            text: r.message,
            type: 'error',
            timeout: 3000
          });
        }
      });
    }
  });

});