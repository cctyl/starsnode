const axios = require('axios');
const path = require('path');
const config = require(path.join(process.cwd(), './config.js'));
module.exports = {

    guid() {
        return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
            var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
            return v.toString(16);
        });
    },
    async getIpInfo() {
        try {
            let {data} = await axios.get('http://ip-api.com/json?lang=zh-CN');
            console.log('getIpInfo Success: ', new Date());
            return data;
        } catch (error) {
            console.log('getIpInfo Error: ', new Date(), error)
        }
    },

    /**
     * 发送告警信息
     * @param messgae
     * @returns {Promise<any>}
     */
    async sendAlert(messgae){
        console.log("sendAlert messgae="+messgae)
        if (!config.warnApi){
            console.log("未配置告警信息，不推送");
            return;
        }
        try {
            const encodedMessage = encodeURIComponent(messgae);
            let {data} = await axios.post(`${config.warnApi}/messages?content=${encodedMessage}`,null,{
                headers: {
                    'token': config.warnToken
                }
            });
            console.log("sendAlert resp="+JSON.stringify(data));

        } catch (error) {
            console.log(`${config.warnApi}/messages?content=${messgae}`)
            console.log('sendAlert Error: ', new Date(), error)
        }
    },
}
