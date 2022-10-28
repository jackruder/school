#version 430

layout (location=0) in vec3 position;
layout (location=1) in vec2 vertexST;

uniform int num_textures;
uniform mat4 v_matrix;
uniform mat4 p_matrix;
uniform mat4 m_matrix[4];

out vec2 fragmentST;
flat out int instance;

void main(void)
{
	gl_Position = p_matrix * v_matrix * m_matrix[gl_InstanceID] * vec4(position,1.0);

	// pass values through
	instance = gl_InstanceID;
	fragmentST = vertexST;
}

