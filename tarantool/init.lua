print("Tarantool KV сервер запущен на порту 3301")

box.once('init_user_v1', function()
    box.schema.user.create('admin', {
        password = 'password',
        if_not_exists = true
    })
    box.schema.user.grant('admin', 'super', nil, nil, {
        if_not_exists = true
    })
    print("Пользователь admin создан")
end)

box.once('init_kv_v1', function()
    box.schema.create_space('KV', {
        if_not_exists = true,
        format = {
            {name = 'key',   type = 'string'},
            {name = 'value', type = 'varbinary', is_nullable = true}
        }
    })
    print("Спейс KV создан")
end)

box.once('init_indexes_v1', function()
    box.space.KV:create_index('primary', {
        type = 'TREE',
        parts = {'key'},
        unique = true,
        if_not_exists = true
    })
    print("Индексы созданы")
end)

function kv_put(key, value)
    if key == nil or key == '' then
        error("Key cannot be empty")
    end
    box.space.KV:put({key, value})
    return true
end

function kv_get(key)
    if key == nil then
        return nil
    end
    local tuple = box.space.KV:get(key)
    if tuple == nil then
        return nil
    end
    return {tuple[1], tuple[2]}
end

function kv_delete(key)
    if key == nil then
        return false
    end
    local deleted = box.space.KV:delete(key)
    return deleted ~= nil
end

function kv_count()
    return box.space.KV:len()
end

function kv_range(key_since, key_to)
    if key_since == nil or key_to == nil then
        error("Range не может быть nil")
    end
    local result = {}
    for _, tuple in box.space.KV.index.primary:pairs(
        key_since, {iterator = 'GE'}
    ) do
        local current_key = tuple[1]
        if current_key > key_to then
            break
        end
        table.insert(result, {current_key, tuple[2]})
    end
    return result
end

rawset(_G, 'kv_put',    kv_put)
rawset(_G, 'kv_get',    kv_get)
rawset(_G, 'kv_delete', kv_delete)
rawset(_G, 'kv_count',  kv_count)
rawset(_G, 'kv_range',  kv_range)

print("Stored procedures загружены")