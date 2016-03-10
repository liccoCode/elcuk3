1. 安装 node.js
curl -sL https://deb.nodesource.com/setup_4.x | sudo -E bash -
sudo apt-get install -y nodejs

2. 安装 overcast
npm -g install overcast

3. pull repo
git pull ssh://git@tig.easyacc.com:21022/wyatt/server-ssh-mgr.git

4. key 文件权限与文件目录
mv server-ssh-mgr ~/.overcast
chmod 600 ~/.overcast/keys/*.key

5. 运行命令测试
overcast ssh es 