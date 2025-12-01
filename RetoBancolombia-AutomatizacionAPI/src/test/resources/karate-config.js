    function fn() {
        karate.configure('connectTimeout', 200000);
        karate.configure('readTimeout', 200000);
        karate.configure('ssl', true);
        karate.configure('retry', {count: 100, interval: 2000});

        const config = karate.callSingle('classpath:features/setup.feature');

        const endPoints = {
            api: 'https://reqres.in',
        }

        const path = {
            PostUser: '/api/users',
            GetUser: '/api/users?page=2',
            PutUser: '/api/users/2?'
        }

        Object.assign(config, endPoints);
        Object.assign(config, path);
        return config;
    }