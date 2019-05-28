

DEFAULT_PARAMS = {
    "allow_redirects": True,
    "timeout": 5,
    "headers": {"Content-Type": "application/json"},
    "auth": ("username", ".........."),
}


def api_call(**params):
    params.update(DEFAULT_PARAMS)
    resp = requests.post("https://api.host.com", **params)
    return resp.json()


def api_call(**params):
    api_params = DEFAULT_PARAMS
    api_params.update(params)
    resp = requests.post("https://api.host.com", **api_params)
    return resp.json()


def foo(bar=[]):
    bar.append(1)
    return bar

foo()
foo()
foo()
