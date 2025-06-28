import { ApiResponse } from '../dto/response/ApiResponseDTO';

function buildHeaders(
  includeContentType = true,
  includeAuth = true
): Record<string, string> {
  const headers: Record<string, string> = {};
  if (includeContentType) {
    headers['Content-Type'] = 'application/json';
  }
  if (includeAuth) {
    const token = localStorage.getItem('auth_token');
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }
  }
  return headers;
}

export async function post<TReq, TRes>(
  url: string,
  body?: TReq,
  includeAuth = true
): Promise<ApiResponse<TRes>> {
  const headers = buildHeaders(true, includeAuth);
  const res = await fetch(url, {
    method: 'POST',
    headers,
    body: body ? JSON.stringify(body) : undefined,
  });

  const response: ApiResponse<TRes> = await res.json();
  if (!res.ok || !response.success) {
    throw new Error(response.error?.message || 'Request failed');
  }

  return response;
}

export async function postFormData<TReq, TRes>(
  url: string,
  form: FormData,
  includeAuth = true
): Promise<ApiResponse<TRes>> {
  const headers = buildHeaders(false, includeAuth);
  const res = await fetch(url, {
    method: 'POST',
    headers,
    body: form,
  });

  const response: ApiResponse<TRes> = await res.json();
  if (!res.ok || !response.success) {
    throw new Error(response.error?.message || 'Request failed');
  }

  return response;
}

export async function put<TReq, TRes>(
  url: string,
  body?: TReq,
  includeAuth = true
): Promise<ApiResponse<TRes>> {
  const headers = buildHeaders(true, includeAuth);
  const res = await fetch(url, {
    method: 'PUT',
    headers,
    body: body ? JSON.stringify(body) : undefined,
  });

  const response: ApiResponse<TRes> = await res.json();
  if (!res.ok || !response.success) {
    throw new Error(response.error?.message || 'Request failed');
  }

  return response;
}


export async function get<T>(url: string, includeAuth = true): Promise<ApiResponse<T>> {
  const headers = buildHeaders(true, includeAuth);

  const res = await fetch(url, {
    headers,
  });

  const response: ApiResponse<T> = await res.json();

  if (!res.ok || !response.success) {
    throw new Error(response.error?.message || 'Request failed');
  }

  return response;
}

export async function del<T>(url: string, includeAuth = true): Promise<ApiResponse<T>> {
  const headers = buildHeaders(false, includeAuth);
  const res = await fetch(url, {
    method: 'DELETE',
    headers,
  });

  const response: ApiResponse<T> = await res.json();
  if (!res.ok || !response.success) {
    throw new Error(response.error?.message || 'Request failed');
  }

  return response;
}

