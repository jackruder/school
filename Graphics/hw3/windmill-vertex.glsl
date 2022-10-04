#version 430

layout (location=0) in vec3 position;
layout (location=1) in vec3 color;
layout (std430, binding=0) buffer bladeM
{
	mat4 bladeMats[];
};

uniform mat4 la_matrix;
uniform mat4 p_matrix;
uniform mat4 bodyM_matrix;

uniform float drawBlades;

out vec4 varyingColor;

void main(void)
{
	if (drawBlades == 0.0f) {
		gl_Position = p_matrix * la_matrix * bodyM_matrix * vec4(position,1.0);
	} else {
		gl_Position = p_matrix * la_matrix * bladeMats[gl_InstanceID] * vec4(position,1.0);
	}	
	varyingColor = vec4(color,1.0);
}

