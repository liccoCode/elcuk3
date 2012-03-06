$(function(){
    $('#plan_list a').click(function(){
        var pid = $(this).attr('pid');
        localStorage.setItem('pid', pid);
        $('#plan_info').load('/procures/planInfo', {pid:pid})
    });

    $('#addpi').click(function(){
        var pid = localStorage.getItem('pid');
        if(!pid){
            alert('请选择一个采购计划!');
            return false;
        }
    });
});